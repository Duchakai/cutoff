#!/bin/bash
# Chạy toàn bộ thí nghiệm. Yêu cầu JDK 17+ (có javac). Chạy từ thư mục gốc của bài.
set -e
mkdir -p build results data
javac -d build src/*.java
java -cp build MergeSort                      # kiểm thử tính đúng đắn
[ -f data/random_1000000.txt ] || java -cp build DataGenerator data
java -Dstdout.encoding=UTF-8 -Xms2g -Xmx2g -cp build Experiment data results/raw_times.csv
python3 scripts/analyze.py results/raw_times.csv results
