本工程通过读取/proc/net/arp的内容获地址映射关系，通过Utils.getLocalIPAddress()获得本地IP地址，若本地IP地址不是192.168.49.1，则本机非GroupOwner，它作为发送者时
则发送到IP地址为192.168.49.1的GroupOwner，若本地地址是192.168.49.1，则发送到ClientIP的终端。


2015.4.22日修改：
主题改为android:Theme.Light.NoTitleBar
并且修改了界面布局
添加了传输成功的文件列表