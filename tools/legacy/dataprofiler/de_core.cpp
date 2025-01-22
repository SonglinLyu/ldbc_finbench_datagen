#include "lgraph/olap_base.h"
#include "./algo.h"

using namespace lgraph_api;
using namespace lgraph_api::olap;

size_t DECore(OlapBase<Empty> & graph, std::set<size_t>& roots) {
    size_t vertices = graph.NumVertices();
    auto active_in = graph.AllocVertexSubset();
    auto active_out = graph.AllocVertexSubset();
    auto diameter = graph.AllocVertexArray<size_t>();
    auto curr = graph.AllocVertexArray<size_t>();
    auto next = graph.AllocVertexArray<size_t>();
    auto vst = graph.AllocVertexArray<size_t>();

    active_in.Fill();
    graph.ProcessVertexActive<size_t>(
            [&](size_t vtx) {
                diameter[vtx] = 0;
                curr[vtx] = 0;
                next[vtx] = 0;
                vst[vtx] = 0;
                return 0;
            },
            active_in);
    assert(roots.size() <= 64);
    active_in.Clear();
    uint64_t full = 0;
    int k = 0;
    for (auto vtx : roots) {
        curr[vtx] |= (1ul << k);
        vst[vtx] |= (1ul << k);
        full |= (1ul << k);
        diameter[vtx] = 0;
        active_in.Add(vtx);
        k++;
    }
    size_t active_vertices = roots.size();

    size_t i_i = 0;
    while (active_vertices > 0) {
        i_i++;
        active_out.Clear();
        active_vertices = graph.ProcessVertexActive<size_t>(
            [&](size_t src) {
                size_t activated = 0;
                for (auto edge : graph.OutEdges(src)) {
                    size_t dst = edge.neighbour;
                    if (vst[dst] != full) {
                        graph.AcquireVertexLock(dst);
                        next[dst] |= curr[src];
                        vst[dst] |= curr[src];
                        if (diameter[dst] != i_i) {
                            diameter[dst] = i_i;
                            active_out.Add(dst);
                            activated++;
                        }
                        graph.ReleaseVertexLock(dst);
                    }
                }
                return activated;
            },
            active_in);
        active_in.Swap(active_out);
        curr.Swap(next);
    }

    roots.clear();
    size_t max_diameter = 0;
    for (size_t vtx = 0; vtx < vertices; vtx++) {
        if (diameter[vtx] > max_diameter) {
            max_diameter = diameter[vtx];
            roots.clear();
        }
        if (diameter[vtx] == max_diameter && roots.size() < 64) {
            roots.insert(vtx);
        }
    }
    return max_diameter;
}
