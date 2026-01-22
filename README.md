方式 1：查看详细进程信息（包含 PID、运行用户、路径）
ps aux | grep mock_device.py

关闭进程
kill -9 pid

后台运行进程
nohup python3 /www/wwwroot/wavesense/mock_device.py
