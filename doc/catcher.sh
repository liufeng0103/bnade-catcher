#!/bin/bash

# catcher启动脚本
# 启动关闭AuctionCatcher
# AuctionCatcher用于获取所有服务器拍卖数据，保存到数据库

#cd /home/xx/catcher
JAVA_OPTION='-Xmx200m'
CLASS_PATH="libs/*:bnade.jar:properties"
RUNNING_FILE="running"
SHUTDOWN_FILE="shutdown"
LOG_DIR="log"
LOG_FILE="catcher.log"

start(){
	# 是否已经运行，查看进程中是否有catcher来判断是否catcher正在运行
	count=`ps -eaf|grep AuctionCatcher|grep catcher|wc -l`
	if [ $count -gt 0 ];then
		echo catcher正在运行, 不再启动新的catcher
		echo catcher正在运行, 不再启动新的catcher>>$LOG_FILE
		exit
	fi
	# 是否已经运行,这种方式当进程意外退出没有及时删除running文件时会有问题，改为通过进程判断
	#if [ -f $RUNNING_FILE ]; then
	#	echo catcher正在运行，不再启动新的catcher
	#	echo catcher正在运行，不再启动新的catcher>>$LOG_FILE
	#	exit
	#fi
	touch $RUNNING_FILE
	
	# 删除关闭文件
	if [ -f $SHUTDOWN_FILE ]; then
		rm $SHUTDOWN_FILE
	fi

	# 保存日志文件
	now=$(date +"%Y-%m-%d-%H.%M.%S")
	if [ ! -d $LOG_DIR ]; then
		mkdir LOG_DIR
	fi
	if [ -f $LOG_FILE ]; then
		mv $LOG_FILE $LOG_DIR/$LOG_FILE.$now
	fi

	nohup java $JAVA_OPTION -cp $CLASS_PATH com.bnade.wow.catcher.AuctionCatcher >>$LOG_FILE 2>>$LOG_FILE &
	
	echo 成功启动catcher
	echo 成功启动catcher>>$LOG_FILE
}

stop(){
	touch $SHUTDOWN_FILE
	echo 正在关闭catcher, 请稍等...
	while :
	do
		count=`ps -eaf|grep AuctionCatcher|grep catcher|wc -l`
		if [ $count -eq 0 ];then
			echo 成功关闭catcher
			echo 成功关闭catcher>>$LOG_FILE
			break
		fi
		sleep 1s
	done
}

if [ $# == 1 ]&&[ $1 = start ]; then
	start
elif [ $# == 1 ]&&[ $1 = stop ]; then
	stop
else
	echo 请输入正确命令 catcher start/stop
fi