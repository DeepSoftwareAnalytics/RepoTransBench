// coding=utf8

// The MIT License
// 
// Copyright (c) 2009 Olle Törnström studiomediatech.com
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
// 
// CREDIT: From an initial implementation in Python by Diogo Kollross, made 
//         publicly available on http://www.geocities.com/diogok_br/lz77.

public class Compressor {

    private final char referencePrefix = '`';
    private final int referencePrefixCode = (int) referencePrefix;
    private final int referenceIntBase = 96;
    private final int referenceIntFloorCode = (int) ' ';
    private final int referenceIntCeilCode = referenceIntFloorCode + referenceIntBase - 1;
    private final int maxStringDistance = referenceIntBase * referenceIntBase - 1;
    private final int minStringLength = 5;
    private final int maxStringLength = referenceIntBase - 1 + minStringLength;
    private final int maxWindowLength = maxStringDistance + minStringLength;
    private final int defaultWindowLength = 144;

    public Compressor() {}

    public String compress(String data) {
        return compress(data, defaultWindowLength);
    }

    public String compress(String data, Integer windowLength) {
        if (windowLength == null) {
            windowLength = defaultWindowLength;
        }

        StringBuilder compressed = new StringBuilder();
        int pos = 0;
        int lastPos = data.length() - minStringLength;

        while (pos < lastPos) {
            int searchStart = Math.max(pos - windowLength, 0);
            int matchLength = minStringLength;
            boolean foundMatch = false;
            int bestMatchDistance = maxStringDistance;
            int bestMatchLength = 0;
            String newCompressed = null;

            while ((searchStart + matchLength) < pos) {
                String m1 = data.substring(searchStart, searchStart + matchLength);
                String m2 = data.substring(pos, pos + matchLength);
                boolean isValidMatch = (m1.equals(m2) && matchLength < maxStringLength);

                if (isValidMatch) {
                    matchLength += 1;
                    foundMatch = true;
                } else {
                    int realMatchLength = matchLength - 1;

                    if (foundMatch && realMatchLength > bestMatchLength) {
                        bestMatchDistance = pos - searchStart - realMatchLength;
                        bestMatchLength = realMatchLength;
                    }

                    matchLength = minStringLength;
                    searchStart += 1;
                    foundMatch = false;
                }
            }

            if (bestMatchLength > 0) {
                newCompressed = referencePrefix + encodeReferenceInt(bestMatchDistance, 2) + encodeReferenceLength(bestMatchLength);
                pos += bestMatchLength;
            } else {
                if (data.charAt(pos) != referencePrefix) {
                    newCompressed = String.valueOf(data.charAt(pos));
                } else {
                    newCompressed = String.valueOf(referencePrefix) + String.valueOf(referencePrefix);
                }
                pos += 1;
            }

            compressed.append(newCompressed);
        }

        return compressed.append(data.substring(pos).replace(String.valueOf(referencePrefix), String.valueOf(referencePrefix) + String.valueOf(referencePrefix))).toString();
    }

    public String decompress(String data) {
        StringBuilder decompressed = new StringBuilder();
        int pos = 0;
        while (pos < data.length()) {
            char currentChar = data.charAt(pos);
            if (currentChar != referencePrefix) {
                decompressed.append(currentChar);
                pos += 1;
            } else {
                char nextChar = data.charAt(pos + 1);
                if (nextChar != referencePrefix) {
                    int distance = decodeReferenceInt(data.substring(pos + 1, pos + 3), 2);
                    int length = decodeReferenceLength(data.charAt(pos + 3));
                    int start = decompressed.length() - distance - length;
                    int end = start + length;
                    decompressed.append(decompressed.substring(start, end));
                    pos += minStringLength - 1;
                } else {
                    decompressed.append(referencePrefix);
                    pos += 2;
                }
            }
        }
        return decompressed.toString();
    }

    private String encodeReferenceInt(int value, int width) {
        if (value >= 0 && value < (int) Math.pow(referenceIntBase, width) - 1) {
            StringBuilder encoded = new StringBuilder();
            while (value > 0) {
                encoded.insert(0, (char) ((value % referenceIntBase) + referenceIntFloorCode));
                value = value / referenceIntBase;
            }

            int missingLength = width - encoded.length();
            for (int i = 0; i < missingLength; i++) {
                encoded.insert(0, (char) referenceIntFloorCode);
            }

            return encoded.toString();
        } else {
            throw new IllegalArgumentException(String.format("Reference value out of range: %d (width = %d)", value, width));
        }
    }

    private String encodeReferenceLength(int length) {
        return encodeReferenceInt(length - minStringLength, 1);
    }

    private int decodeReferenceInt(String data, int width) {
        int value = 0;
        for (int i = 0; i < width; i++) {
            value *= referenceIntBase;
            int charCode = (int) data.charAt(i);
            if (charCode >= referenceIntFloorCode && charCode <= referenceIntCeilCode) {
                value += charCode - referenceIntFloorCode;
            } else {
                throw new IllegalArgumentException(String.format("Invalid char code: %d", charCode));
            }
        }
        return value;
    }

    private int decodeReferenceLength(char data) {
        return decodeReferenceInt(String.valueOf(data), 1) + minStringLength;
    }
}
