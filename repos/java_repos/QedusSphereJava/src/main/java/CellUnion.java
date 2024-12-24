package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CellUnion {
    private List<CellId> cellIds;

    public CellUnion() {
        this.cellIds = new ArrayList<>();
    }

    public CellUnion(List<CellId> cellIds) {
        this(cellIds, true);
    }

    public CellUnion(List<CellId> cellIds, boolean raw) {
        this.cellIds = new ArrayList<>();
        for (CellId cellId : cellIds) {
            this.cellIds.add(cellId);
        }
        if (raw) {
            this.normalize();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CellUnion cellUnion = (CellUnion) obj;
        return cellIds.equals(cellUnion.cellIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellIds);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.getClass().getSimpleName(), cellIds);
    }

    public static CellUnion getUnion(CellUnion x, CellUnion y) {
        List<CellId> combined = new ArrayList<>(x.cellIds);
        combined.addAll(y.cellIds);
        return new CellUnion(combined);
    }

    public static CellUnion getIntersection(CellUnion x, CellId cellId) {
        if (x.contains(cellId)) {
            return new CellUnion(Collections.singletonList(cellId));
        } else {
            int index = Collections.binarySearch(x.cellIds, cellId.rangeMin());
            CellId idMax = cellId.rangeMax();
            List<CellId> intersectedCellIds = new ArrayList<>();
            while (index < x.cellIds.size() && x.cellIds.get(index).compareTo(idMax) <= 0) {
                intersectedCellIds.add(x.cellIds.get(index));
                index++;
            }
            return new CellUnion(intersectedCellIds);
        }
    }

    public static CellUnion getIntersection(CellUnion x, CellUnion y) {
        int i = 0, j = 0;
        List<CellId> cellIds = new ArrayList<>();
        while (i < x.numCells() && j < y.numCells()) {
            CellId xMin = x.cellIds.get(i).rangeMin();
            CellId yMin = y.cellIds.get(j).rangeMin();
            if (xMin.compareTo(yMin) > 0) {
                if (x.cellIds.get(i).compareTo(y.cellIds.get(j).rangeMax()) <= 0) {
                    cellIds.add(x.cellIds.get(i));
                    i++;
                } else {
                    j = Collections.binarySearch(y.cellIds, xMin, j + 1);
                    if (x.cellIds.get(i).compareTo(y.cellIds.get(j - 1).rangeMax()) <= 0) {
                        j--;
                    }
                }
            } else if (yMin.compareTo(xMin) > 0) {
                if (y.cellIds.get(j).compareTo(x.cellIds.get(i).rangeMax()) <= 0) {
                    cellIds.add(y.cellIds.get(j));
                    j++;
                } else {
                    i = Collections.binarySearch(x.cellIds, yMin, i + 1);
                    if (y.cellIds.get(j).compareTo(x.cellIds.get(i - 1).rangeMax()) <= 0) {
                        i--;
                    }
                }
            } else {
                if (x.cellIds.get(i).compareTo(y.cellIds.get(j)) < 0) {
                    cellIds.add(x.cellIds.get(i));
                    i++;
                } else {
                    cellIds.add(y.cellIds.get(j));
                    j++;
                }
            }
        }

        CellUnion cellUnion = new CellUnion(cellIds);
        assert cellUnion.isNormalized();
        return cellUnion;
    }

    public void expand(int level) {
        List<CellId> output = new ArrayList<>();
        long levelLsb = CellId.lsbForLevel(level);
        int i = numCells() - 1;
        while (i >= 0) {
            CellId cellId = cellIds.get(i);
            if (cellId.lsb() < levelLsb) {
                cellId = cellId.parent(level);
                while (i > 0 && cellId.contains(cellIds.get(i - 1))) {
                    i--;
                }
            }
            output.add(cellId);
            cellId.appendAllNeighbors(level, output);
            i--;
        }
        cellIds = output;
    }

    public void expand(Angle minRadius, int maxLevelDiff) {
        int minLevel = CellId.MAX_LEVEL;
        for (CellId cellId : cellIds) {
            minLevel = Math.min(minLevel, cellId.level());
        }

        int radiusLevel = CellId.minWidth().getMaxLevel(minRadius.radians());
        if (radiusLevel == 0 && minRadius.radians() > CellId.minWidth().getValue(0)) {
            expand(0);
        }
        expand(Math.min(minLevel + maxLevelDiff, radiusLevel));
    }

    public static CellUnion getDifference(CellUnion x, CellUnion y) {
        List<CellId> cellIds = new ArrayList<>();
        for (CellId cellId : x.cellIds) {
            getDifference(cellId, y, cellIds);
        }

        CellUnion cellUnion = new CellUnion(cellIds);
        assert cellUnion.isNormalized();
        return cellUnion;
    }

    private static void getDifference(CellId cellId, CellUnion y, List<CellId> cellIds) {
        if (!y.intersects(cellId)) {
            cellIds.add(cellId);
        } else if (!y.contains(cellId)) {
            for (CellId child : cellId.children()) {
                getDifference(child, y, cellIds);
            }
        }
    }

    public int numCells() {
        return cellIds.size();
    }

    public CellId getCellId(int i) {
        return cellIds.get(i);
    }

    public List<CellId> getCellIds() {
        return cellIds;
    }

    public boolean normalize() {
        Collections.sort(cellIds);
        List<CellId> output = new ArrayList<>();
        for (CellId cellId : cellIds) {
            if (!output.isEmpty() && output.get(output.size() - 1).contains(cellId)) {
                continue;
            }

            while (!output.isEmpty() && cellId.contains(output.get(output.size() - 1))) {
                output.remove(output.size() - 1);
            }

            while (output.size() >= 3) {
                CellId a = output.get(output.size() - 3);
                CellId b = output.get(output.size() - 2);
                CellId c = output.get(output.size() - 1);
                if ((a.id() ^ b.id() ^ c.id()) != cellId.id()) {
                    break;
                }

                long mask = cellId.lsb() << 1;
                mask = ~(mask + (mask << 1));
                long idMasked = (cellId.id() & mask);
                if ((a.id() & mask) != idMasked || (b.id() & mask) != idMasked || (c.id() & mask) != idMasked || cellId.isFace()) {
                    break;
                }

                output.remove(output.size() - 1);
                output.remove(output.size() - 1);
                output.remove(output.size() - 1);
                cellId = cellId.parent();
            }

            output.add(cellId);
        }

        if (output.size() < numCells()) {
            cellIds = output;
            return true;
        }
        return false;
    }

    public List<CellId> denormalize(int minLevel, int levelMod) {
        assert minLevel >= 0;
        assert minLevel <= CellId.MAX_LEVEL;
        assert levelMod >= 1;
        assert levelMod <= 3;

        List<CellId> denormalized = new ArrayList<>();
        for (CellId cellId : cellIds) {
            int level = cellId.level();
            int newLevel = Math.max(minLevel, level);
            if (levelMod > 1) {
                newLevel += (CellId.MAX_LEVEL - (newLevel - minLevel)) % levelMod;
                newLevel = Math.min(CellId.MAX_LEVEL, newLevel);
            }
            if (newLevel == level) {
                denormalized.add(cellId);
            } else {
                denormalized.addAll(cellId.children(newLevel));
            }
        }
        return denormalized;
    }

    public boolean contains(Object obj) {
        if (obj instanceof Cell) {
            return contains(((Cell) obj).getId());
        } else if (obj instanceof CellId) {
            CellId cellId = (CellId) obj;
            int index = Collections.binarySearch(cellIds, cellId);
            if (index < cellIds.size() && cellIds.get(index).rangeMin().compareTo(cellId) <= 0) {
                return true;
            }
            return index != 0 && cellIds.get(index - 1).rangeMax().compareTo(cellId) >= 0;
        } else if (obj instanceof Point) {
            return contains(CellId.fromPoint((Point) obj));
        } else if (obj instanceof CellUnion) {
            CellUnion cellUnion = (CellUnion) obj;
            for (int i = 0; i < cellUnion.numCells(); i++) {
                if (!contains(cellUnion.getCellId(i))) {
                    return false;
                }
            }
            return true;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean intersects(Object obj) {
        if (obj instanceof CellId) {
            CellId cellId = (CellId) obj;
            int index = Collections.binarySearch(cellIds, cellId);
            if (index != cellIds.size() && cellIds.get(index).rangeMin().compareTo(cellId.rangeMax()) <= 0) {
                return true;
            }
            return index != 0 && cellIds.get(index - 1).rangeMax().compareTo(cellId.rangeMin()) >= 0;
        } else if (obj instanceof CellUnion) {
            CellUnion cellUnion = (CellUnion) obj;
            for (CellId cellId : cellUnion.cellIds) {
                if (intersects(cellId)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private boolean isNormalized() {
        for (int i = 0; i < numCells() - 1; i++) {
            if (cellIds.get(i).compareTo(cellIds.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }
}
