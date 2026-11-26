"""Phân tích kết quả: thống kê theo (dataset, cutoff), chọn cutoff tốt nhất, vẽ đồ thị."""
import sys, math
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

raw, outdir = sys.argv[1], sys.argv[2]
df = pd.read_csv(raw)
df["ms"] = df["nanos"] / 1e6

def ci95(s):
    return 1.96 * s.std(ddof=1) / math.sqrt(len(s))

g = df.groupby(["dataset", "n", "cutoff"])["ms"]
st = g.agg(trials="count", mean="mean", median="median", std="std", min="min").reset_index()
st["ci95"] = g.apply(ci95).values
st["rel_to_c1_pct"] = 0.0
rows = []
for ds, sub in st.groupby("dataset"):
    base = sub.loc[sub.cutoff == 1, "median"].iloc[0]
    st.loc[sub.index, "rel_to_c1_pct"] = (sub["median"] / base - 1) * 100
st.to_csv(f"{outdir}/summary.csv", index=False, float_format="%.4f")

# Cutoff tốt nhất theo median, và "vùng phẳng": cutoff có median <= 1% chậm hơn best
best_rows = []
for ds, sub in st.groupby("dataset"):
    b = sub.loc[sub["median"].idxmin()]
    flat = sub[sub["median"] <= b["median"] * 1.01]["cutoff"].tolist()
    c7 = sub.loc[sub.cutoff == 7, "median"].iloc[0]
    c10 = sub.loc[sub.cutoff == 10, "median"].iloc[0]
    c1 = sub.loc[sub.cutoff == 1, "median"].iloc[0]
    best_rows.append(dict(dataset=ds, n=int(b["n"]), best_cutoff=int(b["cutoff"]),
        best_ms=b["median"], flat_region_1pct=f"{min(flat)}-{max(flat)}" if flat else "",
        c1_ms=c1, c7_ms=c7, c10_ms=c10,
        gain_vs_c1_pct=(1 - b["median"] / c1) * 100,
        c7_vs_best_pct=(c7 / b["median"] - 1) * 100,
        c10_vs_best_pct=(c10 / b["median"] - 1) * 100))
best = pd.DataFrame(best_rows).sort_values(["dataset"])
best.to_csv(f"{outdir}/best_cutoff.csv", index=False, float_format="%.4f")
print(best.to_string(index=False, float_format=lambda x: f"{x:.3f}"))

# Đồ thị 1: thời gian chuẩn hóa theo cutoff, các cỡ n ngẫu nhiên
plt.figure(figsize=(8, 5))
for ds in ["random_1000", "random_10000", "random_100000", "random_1000000"]:
    sub = st[st.dataset == ds].sort_values("cutoff")
    plt.plot(sub.cutoff, sub["median"] / sub["median"].min(), marker="o", ms=3, label=ds.replace("random_", "n="))
plt.axvline(7, ls=":", c="gray"); plt.axvline(10, ls=":", c="gray")
plt.xscale("log", base=2); plt.xlabel("CUTOFF (log2)"); plt.ylabel("Thời gian median / thời gian tốt nhất")
plt.title("Dữ liệu ngẫu nhiên: ảnh hưởng của CUTOFF"); plt.grid(alpha=.3); plt.legend()
plt.tight_layout(); plt.savefig(f"{outdir}/fig1_random_by_n.png", dpi=150); plt.close()

# Đồ thị 2: n = 10^6, thời gian tuyệt đối có khoảng tin cậy 95%
plt.figure(figsize=(8, 5))
sub = st[st.dataset == "random_1000000"].sort_values("cutoff")
plt.errorbar(sub.cutoff, sub["mean"], yerr=sub["ci95"], marker="o", ms=3, capsize=2)
plt.xscale("log", base=2); plt.xlabel("CUTOFF (log2)"); plt.ylabel("Thời gian trung bình (ms)")
plt.title("n = 10^6, ngẫu nhiên: trung bình ± KTC 95%"); plt.grid(alpha=.3)
plt.tight_layout(); plt.savefig(f"{outdir}/fig2_random_1e6_ci.png", dpi=150); plt.close()

# Đồ thị 3: các dạng dữ liệu n = 10^6
plt.figure(figsize=(8, 5))
for ds in ["random_1000000", "sorted_1000000", "reversed_1000000", "nearlysorted_1000000", "duplicates_1000000"]:
    sub = st[st.dataset == ds].sort_values("cutoff")
    plt.plot(sub.cutoff, sub["median"] / sub["median"].min(), marker="o", ms=3, label=ds.replace("_1000000", ""))
plt.xscale("log", base=2); plt.xlabel("CUTOFF (log2)"); plt.ylabel("Thời gian median / thời gian tốt nhất")
plt.title("n = 10^6: các dạng dữ liệu"); plt.grid(alpha=.3); plt.legend()
plt.tight_layout(); plt.savefig(f"{outdir}/fig3_data_types.png", dpi=150); plt.close()

# So sánh cặp: trong cùng một lượt (trial), hiệu thời gian giữa cutoff c và cutoff chuẩn 10
import numpy as np
from scipy import stats
rows = []
for ds in ["random_1000", "random_10000", "random_100000", "random_1000000"]:
    sub = df[df.dataset == ds]
    piv = sub.pivot(index="trial", columns="cutoff", values="ms")
    for c in [1, 7, 12, 16, 20, 24, 28, 32, 40, 64]:
        d = (piv[c] - piv[10]).values            # <0: c nhanh hơn cutoff 10
        t, p = stats.ttest_rel(piv[c], piv[10])
        w = stats.wilcoxon(d).pvalue
        rows.append(dict(dataset=ds, cutoff=c, mean_diff_ms=d.mean(),
                         pct_vs_c10=d.mean() / piv[10].mean() * 100,
                         ci95_ms=1.96 * d.std(ddof=1) / math.sqrt(len(d)), p_ttest=p, p_wilcoxon=w))
pd.DataFrame(rows).to_csv(f"{outdir}/paired_vs_cutoff10.csv", index=False, float_format="%.5f")
print(pd.DataFrame(rows).round(4).to_string(index=False))
