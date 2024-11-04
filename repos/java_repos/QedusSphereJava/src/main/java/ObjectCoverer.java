package com.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class ObjectCoverer {

    private int minLevel = 0;
    private int maxLevel = CellId.MAX_LEVEL;
    private int levelMod = 1;
    private int maxCells = 8;
    private Object region = null;
    private List<CellId> result;
    private PriorityQueue<Candidate> pq;
    private boolean interiorCovering;

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        assert minLevel >= 0 && minLevel <= CellId.MAX_LEVEL;
        this.minLevel = Math.max(0, Math.min(CellId.MAX_LEVEL, minLevel));
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        assert maxLevel >= 0 && maxLevel <= CellId.MAX_LEVEL;
        this.maxLevel = Math.max(0, Math.min(CellId.MAX_LEVEL, maxLevel));
    }

    public int getLevelMod() {
        return levelMod;
    }

    public void setLevelMod(int levelMod) {
        assert levelMod >= 1 && levelMod <= 3;
        this.levelMod = Math.max(1, Math.min(3, levelMod));
    }

    public int getMaxCells() {
        return maxCells;
    }

    public void setMaxCells(int maxCells) {
        this.maxCells = maxCells;
    }

    public List<CellId> getCovering(Object region) {
        this.result = new ArrayList<>();
        CellUnion tmpUnion = getCellUnion(region);
        return tmpUnion.denormalize(minLevel, levelMod);
    }

    public List<CellId> getInteriorCovering(Object region) {
        this.result = new ArrayList<>();
        CellUnion tmpUnion = getInteriorCellUnion(region);
        return tmpUnion.denormalize(minLevel, levelMod);
    }

    private Candidate newCandidate(Cell cell) {
        if (!region.mayIntersect(cell)) {
            return null;
        }
        boolean isTerminal = false;
        if (cell.level() >= minLevel) {
            if (interiorCovering) {
                if (region.contains(cell)) {
                    isTerminal = true;
                } else if (cell.level() + levelMod > maxLevel) {
                    return null;
                }
            } else {
                if (cell.level() + levelMod > maxLevel || region.contains(cell)) {
                    isTerminal = true;
                }
            }
        }

        Candidate candidate = new Candidate();
        candidate.cell = cell;
        candidate.isTerminal = isTerminal;
        candidate.children = new ArrayList<>();
        return candidate;
    }

    private int maxChildrenShift() {
        return 2 * levelMod;
    }

    private int expandChildren(Candidate candidate, Cell cell, int numLevels) {
        numLevels -= 1;
        int numTerminals = 0;
        for (Cell childCell : cell.subdivide()) {
            if (numLevels > 0) {
                if (region.mayIntersect(childCell)) {
                    numTerminals += expandChildren(candidate, childCell, numLevels);
                }
                continue;
            }
            Candidate child = newCandidate(childCell);
            if (child != null) {
                candidate.children.add(child);
                if (child.isTerminal) {
                    numTerminals += 1;
                }
            }
        }
        return numTerminals;
    }

    private void addCandidate(Candidate candidate) {
        if (candidate == null) {
            return;
        }

        if (candidate.isTerminal) {
            result.add(candidate.cell.id());
            return;
        }

        assert candidate.numChildren() == 0;

        int numLevels = levelMod;
        if (candidate.cell.level() < minLevel) {
            numLevels = 1;
        }
        int numTerminals = expandChildren(candidate, candidate.cell, numLevels);

        if (candidate.numChildren() == 0) {
            // Not needed due to GC
        } else if (!interiorCovering && numTerminals == 1 << maxChildrenShift() && candidate.cell.level() >= minLevel) {
            candidate.isTerminal = true;
            addCandidate(candidate);
        } else {
            int priority = (((candidate.cell.level() << maxChildrenShift()) + candidate.numChildren()) << maxChildrenShift()) + numTerminals;
            pq.add(candidate);
        }
    }

    private void getInitialCandidates() {
        if (maxCells >= 4) {
            Cap cap = region.getCapBound();
            int level = Math.min(CellId.minWidth().getMaxLevel(2 * cap.angle().radians()), Math.min(maxLevel, CellId.MAX_LEVEL - 1));

            if (levelMod > 1 && level > minLevel) {
                level -= (level - minLevel) % levelMod;
            }

            if (level > 0) {
                CellId cellId = CellId.fromPoint(cap.axis());
                List<CellId> base = cellId.getVertexNeighbors(level);
                for (CellId baseId : base) {
                    addCandidate(newCandidate(new Cell(baseId)));
                }
                return;
            }
        }

        for (int face = 0; face < 6; face++) {
            addCandidate(newCandidate(FACE_CELLS[face]));
        }
    }

    // private void getCovering(Object region) {
    //     assert pq.isEmpty();
    //     assert result.isEmpty();
    //     this.region = region;

    //     getInitialCandidates();
    //     while (!pq.isEmpty() && (!interiorCovering || result.size() < maxCells)) {
    //         Candidate candidate = pq.poll();

    //         int resultSize;
    //         if (interiorCovering) {
    //             resultSize = 0;
    //         } else {
    //             resultSize = pq.size();
    //         }

    //         if (candidate.cell.level() < minLevel || candidate.numChildren() == 1 || result.size() + resultSize + candidate.numChildren() <= maxCells) {
    //             for (Candidate child : candidate.children) {
    //                 addCandidate(child);
    //             }
    //         } else if (interiorCovering) {
    //             // Do nothing here
    //         } else {
    //             candidate.isTerminal = true;
    //             addCandidate(candidate);
    //         }
    //     }

    //     pq.clear();
    //     region = null;
    // }

    private CellUnion getCellUnion(Object region) {
        this.interiorCovering = false;
        getCovering(region);
        return new CellUnion(result);
    }

    private CellUnion getInteriorCellUnion(Object region) {
        this.interiorCovering = true;
        getCovering(region);
        return new CellUnion(result);
    }

    public static Iterable<CellId> floodFill(Object region, CellId start) {
        Set<CellId> allNbrs = new HashSet<>();
        List<CellId> frontier = new ArrayList<>();
        allNbrs.add(start);
        frontier.add(start);
        while (!frontier.isEmpty()) {
            CellId cellId = frontier.remove(frontier.size() - 1);
            if (!region.mayIntersect(new Cell(cellId))) {
                continue;
            }
            ObjectCoverer.yield(cellId);

            List<CellId> neighbors = cellId.getEdgeNeighbors();
            for (CellId nbr : neighbors) {
                if (!allNbrs.contains(nbr)) {
                    allNbrs.add(nbr);
                    frontier.add(nbr);
                }
            }
        }
    }

    public static List<CellId> getSimpleCovering(Object region, Point start, int level) {
        return floodFill(region, CellId.fromPoint(start).parent(level)).toList();
    }

    public static class Candidate implements Comparable<Candidate> {
        private Cell cell;
        private boolean isTerminal;
        private List<Candidate> children;

        public int numChildren() {
            return children.size();
        }

        @Override
        public int compareTo(Candidate other) {
            return this.cell.id().compareTo(other.cell.id());
        }
    }

    private void yield(CellId cellId) {
        // Implement the logic to yield cellId
        // Placeholder, replace with actual implementation
    }

    private static final Cell[] FACE_CELLS = {
        // Initialize FACE_CELLS array
    };
}