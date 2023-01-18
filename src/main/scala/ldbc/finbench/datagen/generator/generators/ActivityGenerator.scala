package ldbc.finbench.datagen.generator.generators

import ldbc.finbench.datagen.entities.nodes.{Account, Company, Loan, Medium, Person}
import ldbc.finbench.datagen.generator.DatagenParams
import ldbc.finbench.datagen.generator.events.{
  CompanyGuaranteeEvent,
  CompanyInvestEvent,
  CompanyLoanEvent,
  CompanyRegisterEvent,
  PersonGuaranteeEvent,
  PersonInvestEvent,
  PersonLoanEvent,
  PersonRegisterEvent,
  SignInEvent,
  SubEvents,
  TransferEvent,
  WithdrawEvent,
  WorkInEvent
}
import org.apache.spark.rdd.RDD
import java.util

import ldbc.finbench.datagen.entities.edges.{
  CompanyApplyLoan,
  CompanyGuaranteeCompany,
  CompanyInvestCompany,
  CompanyOwnAccount,
  Deposit,
  PersonApplyLoan,
  PersonGuaranteePerson,
  PersonInvestCompany,
  PersonOwnAccount,
  Repay,
  SignIn,
  Transfer,
  Withdraw,
  WorkIn
}
import ldbc.finbench.datagen.model.raw.{CompanyApplyLoanRaw, DepositRaw}
import ldbc.finbench.datagen.util.GeneratorConfiguration
import org.apache.spark.TaskContext

import scala.collection.JavaConverters._
import scala.collection.SortedMap
import scala.util.Random

class ActivityGenerator(conf: GeneratorConfiguration) extends Serializable {
  val blockSize = DatagenParams.blockSize

  def personRegisterEvent(personRDD: RDD[Person]): RDD[PersonOwnAccount] = {
    val blocks = personRDD.zipWithUniqueId().map { case (v, k) => (k / blockSize, (k, v)) }

    val personOwnAccount = blocks
      .combineByKeyWithClassTag(
        personByRank => SortedMap(personByRank),
        (map: SortedMap[Long, Person], personByRank) => map + personByRank,
        (a: SortedMap[Long, Person], b: SortedMap[Long, Person]) => a ++ b
      )
      .mapPartitions(groups => {
        val personRegisterGen = new PersonRegisterEvent

        val personRegisterGroups = for { (block, persons) <- groups } yield {
          val personList = new util.ArrayList[Person](persons.size)
          for (p <- persons.values) { personList.add(p) }

          personRegisterGen.personRegister(personList, block.toInt, conf)
        }

        for {
          personOwnAccounts <- personRegisterGroups
          personOwnAccount  <- personOwnAccounts.iterator().asScala
        } yield personOwnAccount
      })
    personOwnAccount
  }

  def companyRegisterEvent(companyRDD: RDD[Company]): RDD[CompanyOwnAccount] = {
    val blocks = companyRDD
      .zipWithUniqueId()
      .map(row => (row._2, row._1))
      .map { case (k, v) => (k / blockSize, (k, v)) }

    val companyOwnAccount =
      blocks
        .combineByKeyWithClassTag(
          companyById => SortedMap(companyById),
          (map: SortedMap[Long, Company], companyById) => map + companyById,
          (a: SortedMap[Long, Company], b: SortedMap[Long, Company]) => a ++ b
        )
        .mapPartitions(groups => {
          val companyRegisterGen = new CompanyRegisterEvent

          val companyRegisterGroups = for { (block, companies) <- groups } yield {
            val companyList = new util.ArrayList[Company](companies.size)
            for (p <- companies.values) { companyList.add(p) }
            companyRegisterGen.companyRegister(companyList, block.toInt, conf)
          }

          for {
            companyOwnAccounts <- companyRegisterGroups
            companyOwnAccount  <- companyOwnAccounts.iterator().asScala
          } yield companyOwnAccount
        })
    companyOwnAccount
  }

  def personInvestEvent(personRDD: RDD[Person],
                        companyRDD: RDD[Company]): RDD[PersonInvestCompany] = {

    val companyListSize = 5000 // todo config the company list size
    val fraction        = companyListSize.toDouble / companyRDD.count()

    val personParts       = personRDD.partitions.length
    val companySampleList = new util.ArrayList[util.List[Company]](personParts)
    for (i <- 1 to personParts) {
      companySampleList.add(companyRDD.sample(true, fraction).collect().toList.asJava)
    }

    val personInvestRels = personRDD.mapPartitions(persons => {
      val personList = new util.ArrayList[Person]()
      if (persons.hasNext) {
        personList.add(persons.next())
      }

      val personInvestGenerator = new PersonInvestEvent()

      val part = TaskContext.getPartitionId()

      val personInvestList =
        personInvestGenerator.personInvest(personList, companySampleList.get(part), part)

      for {
        personInvest <- personInvestList.iterator().asScala
      } yield personInvest
    })

    personInvestRels
  }

  def companyInvestEvent(companyRDD: RDD[Company]): RDD[CompanyInvestCompany] = {
    val companyInvestRels = companyRDD.mapPartitions(companies => {
      val companyList = new util.ArrayList[Company]()
      if (companies.hasNext) {
        companyList.add(companies.next())
      }
      val companyInvestGenerator = new CompanyInvestEvent()

      val companyInvestList =
        companyInvestGenerator.companyInvest(companyList, TaskContext.get.partitionId)

      for {
        companyInvest <- companyInvestList.iterator().asScala
      } yield companyInvest
    })

    companyInvestRels
  }

  def workInEvent(personRDD: RDD[Person], companyRDD: RDD[Company]): RDD[WorkIn] = {
    val companyListSize = 5000 // todo config the company list size
    val fraction        = companyListSize.toDouble / companyRDD.count()

    val personParts       = personRDD.partitions.length
    val companySampleList = new util.ArrayList[util.List[Company]](personParts)
    for (i <- 1 to personParts) {
      companySampleList.add(companyRDD.sample(true, fraction).collect().toList.asJava)
    }

    val personWorkInRels = personRDD.mapPartitions(persons => {
      val personList = new util.ArrayList[Person]()
      if (persons.hasNext) {
        personList.add(persons.next())
      }

      val personWorkInGenerator = new WorkInEvent()
      val part                  = TaskContext.getPartitionId()
      val personWorkInList =
        personWorkInGenerator.workIn(personList, companySampleList.get(part), part)

      for {
        personWorkIn <- personWorkInList.iterator().asScala
      } yield personWorkIn
    })

    personWorkInRels
  }

  def signEvent(mediumRDD: RDD[Medium], accountRDD: RDD[Account]): RDD[SignIn] = {
    val accountListSize = 5000 // todo config the company list size
    val fraction        = accountListSize.toDouble / accountRDD.count()

    val mediumParts       = mediumRDD.partitions.length
    val accountSampleList = new util.ArrayList[util.List[Account]](mediumParts)
    for (i <- 1 to mediumParts) {
      accountSampleList.add(accountRDD.sample(true, fraction).collect().toList.asJava)
    }

    val signRels = mediumRDD.mapPartitions(mediums => {
      val mediumList = new util.ArrayList[Medium]()
      if (mediums.hasNext) {
        mediumList.add(mediums.next())
      }

      val signGenerator = new SignInEvent()
      val part          = TaskContext.getPartitionId()
      val signInList =
        signGenerator.signIn(mediumList, accountSampleList.get(part), part, conf)

      for {
        signIn <- signInList.iterator().asScala
      } yield signIn
    })

    signRels
  }

  def personGuaranteeEvent(personRDD: RDD[Person]): RDD[PersonGuaranteePerson] = {
    personRDD.mapPartitions(persons => {
      val personGuaranteeEvent = new PersonGuaranteeEvent

      val personList = new util.ArrayList[Person]()
      if (persons.hasNext) {
        personList.add(persons.next())
      }
      val guaranteeList =
        personGuaranteeEvent.personGuarantee(personList, TaskContext.getPartitionId())
      for {
        guarantee <- guaranteeList.iterator().asScala
      } yield guarantee
    })
  }

  def companyGuaranteeEvent(companyRDD: RDD[Company]): RDD[CompanyGuaranteeCompany] = {
    companyRDD.mapPartitions(companies => {
      val companyGuaranteeEvent = new CompanyGuaranteeEvent

      val companyList = new util.ArrayList[Company]()
      if (companies.hasNext) {
        companyList.add(companies.next())
      }
      val guaranteeList =
        companyGuaranteeEvent.companyGuarantee(companyList, TaskContext.getPartitionId())
      for {
        guarantee <- guaranteeList.iterator().asScala
      } yield guarantee
    })
  }

  def personLoanEvent(personRDD: RDD[Person]): RDD[PersonApplyLoan] = {
    personRDD.mapPartitions(persons => {
      val personLoanEvent = new PersonLoanEvent
      val personList      = new util.ArrayList[Person]()
      if (persons.hasNext) {
        personList.add(persons.next())
      }
      val loanList = personLoanEvent.personLoan(personList, TaskContext.getPartitionId(), conf)
      for { applyLoan <- loanList.iterator().asScala } yield applyLoan
    })
  }

  def companyLoanEvent(companyRDD: RDD[Company]): RDD[CompanyApplyLoan] = {
    companyRDD.mapPartitions(companies => {
      val companyLoanEvent = new CompanyLoanEvent

      val companyList = new util.ArrayList[Company]()
      if (companies.hasNext) {
        companyList.add(companies.next())
      }
      val loanList =
        companyLoanEvent.companyLoan(companyList, TaskContext.getPartitionId(), conf)
      for {
        applyLoan <- loanList.iterator().asScala
      } yield applyLoan
    })
  }

  def transferEvent(accountRDD: RDD[Account]): RDD[Transfer] = {
    accountRDD.mapPartitions(accounts => {
      val transferEvent = new TransferEvent
      val accountList   = new util.ArrayList[Account]()
      if (accounts.hasNext) {
        accountList.add(accounts.next())
      }
      val transferList = transferEvent.transfer(accountList, TaskContext.getPartitionId())
      for { transfer <- transferList.iterator().asScala } yield transfer
    })
  }

  def withdrawEvent(accountRDD: RDD[Account]): RDD[Withdraw] = {
    accountRDD.mapPartitions(accounts => {
      val withdrawEvent = new WithdrawEvent
      val accountList   = new util.ArrayList[Account]()
      if (accounts.hasNext) {
        accountList.add(accounts.next())
      }
      val withdrawList = withdrawEvent.withdraw(accountList, TaskContext.getPartitionId())
      for { withdraw <- withdrawList.iterator().asScala } yield withdraw
    })
  }

  def depositEvent(loanRDD: RDD[Loan], accountRDD: RDD[Account]): RDD[Deposit] = {
    val accountListSize = 5000 // todo config the company list size
    val fraction        = accountListSize.toDouble / accountRDD.count()

    val loanParts         = loanRDD.partitions.length
    val accountSampleList = new util.ArrayList[util.List[Account]](loanParts)
    for (i <- 1 to loanParts) {
      accountSampleList.add(accountRDD.sample(true, fraction).collect().toList.asJava)
    }

    val depositRels = loanRDD.mapPartitions(loans => {
      val loanList = new util.ArrayList[Loan]()
      if (loans.hasNext) {
        loanList.add(loans.next())
      }

      val subEventsGenerator = new SubEvents()
      val part               = TaskContext.getPartitionId()
      val depositList =
        subEventsGenerator.subEventDeposit(loanList, accountSampleList.get(part), part)

      for {
        deposit <- depositList.iterator().asScala
      } yield deposit
    })
    depositRels
  }

  def repayEvent(accountRDD: RDD[Account], loanRDD: RDD[Loan]): RDD[Repay] = {
    val loanListSize = 5000 // todo config the company list size
    val fraction     = loanListSize.toDouble / loanRDD.count()

    val accountParts   = accountRDD.partitions.length
    val loanSampleList = new util.ArrayList[util.List[Loan]](accountParts)
    for (i <- 1 to accountParts) {
      loanSampleList.add(loanRDD.sample(true, fraction).collect().toList.asJava)
    }

    val repayRels = accountRDD.mapPartitions(accounts => {
      val accountList = new util.ArrayList[Account]()
      if (accounts.hasNext) {
        accountList.add(accounts.next())
      }

      val subEventsGenerator = new SubEvents()
      val part               = TaskContext.getPartitionId()
      val repayList =
        subEventsGenerator.subEventRepay(accountList, loanSampleList.get(part), part)

      for {
        repay <- repayList.iterator().asScala
      } yield repay
    })

    repayRels
  }

}
