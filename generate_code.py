#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
تولید کد فعال‌سازی رایدو (هم‌خوان با KeyHelper.java)
فرمت: XXXX-XXXX  |  اعتبار: ۳۰ روز از لحظه فعال‌سازی روی دستگاه
"""
from __future__ import annotations
import argparse
import hashlib
import secrets
import sys

SECRET = "RAIDO-ACT-2026-v2"
MASTER_PHRASE = "Milad@Raido"
ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"


def normalize(s: str) -> str:
    return "".join(c for c in s.upper() if c not in "- _")


def signature2(body: str) -> str:
    dig = hashlib.sha256(f"{body}|{SECRET}".encode()).digest()
    return ALPHABET[dig[0] % len(ALPHABET)] + ALPHABET[dig[1] % len(ALPHABET)]


def body_from_seed(seed: str) -> str:
    dig = hashlib.sha256(seed.encode()).digest()
    out = []
    i = 0
    while len(out) < 6:
        out.append(ALPHABET[dig[i % len(dig)] % len(ALPHABET)])
        i += 1
    return "".join(out)


def make_code_from_seed(seed: str) -> str:
    body = body_from_seed(seed)
    return body + signature2(body)


def make_random_code() -> str:
    # 6 random chars from alphabet + 2 signature
    body = "".join(secrets.choice(ALPHABET) for _ in range(6))
    return body + signature2(body)


def format_display(code8: str) -> str:
    n = normalize(code8)
    return f"{n[:4]}-{n[4:8]}" if len(n) == 8 else n


def master_code() -> str:
    return make_code_from_seed(f"{MASTER_PHRASE}|{SECRET}|MASTER")


def is_valid(code: str) -> bool:
    n = normalize(code)
    if len(n) != 8:
        return False
    return n[6:8] == signature2(n[:6])


def main():
    ap = argparse.ArgumentParser(description="RAIDO activation code generator")
    ap.add_argument("-n", "--count", type=int, default=1, help="تعداد کد عادی")
    ap.add_argument("--master", action="store_true", help="نمایش کد مستر نامحدود")
    ap.add_argument("--check", type=str, default="", help="اعتبارسنجی یک کد")
    args = ap.parse_args()

    if args.check:
        n = normalize(args.check)
        print("normalized:", n)
        print("valid_signed:", is_valid(n))
        print("is_master:", n == normalize(master_code()))
        print("display:", format_display(n))
        return

    if args.master or args.count == 0:
        m = master_code()
        print("MASTER (unlimited):", format_display(m))
        print("  raw:", m)
        if not args.count:
            return

    print(f"Generating {args.count} code(s) — 30 days from activation on device:\n")
    for i in range(args.count):
        c = make_random_code()
        print(f"  {i+1:3d}.  {format_display(c)}")


if __name__ == "__main__":
    main()
