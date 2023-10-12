@echo off
chcp 65001
ping %1
tracert -w 1 %1