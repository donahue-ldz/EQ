package com.lzw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.Stack;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.InternationalFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.lzw.dao.Dao;
import com.lzw.frame.TelFrame;
import com.lzw.system.Resource;
import com.lzw.userList.ChatTree;
import com.lzw.userList.User;

public class EQ extends Dialog {
	
	private JTextField ipEndTField;    //起始IP
	private JTextField ipStartTField;  //终止Ip
	private JTextField userNameTField;  //用户名文本框
	private JPasswordField passwordTField;  //密码
	private JTextField placardPathTField;  //系统公告文本框
	private JTextField updatePathTField;   //程序升级文本框
	private JTextField pubPathTField;   //公共程序文本框
	public static EQ frame = null;      //主窗体对象
	private ChatTree chatTree;          //用户树对象
	private JPopupMenu popupMenu;        //弹出菜单选项，点击右键时候弹出来的选项
	private JTabbedPane tabbedPane;    //选项卡
	private JToggleButton searchUserButton; //搜索用户按钮
	private JProgressBar progressBar;   //搜索进度条
	private JList faceList;    //搜索列表
	private JButton selectInterfaceOKButton;   //界面选择按钮
	private DatagramSocket ss;    //数据通信包
	private final JLabel stateLabel;   //状态栏标签
	private static String user_dir;   //用户当前文件夹
	private static File localFile;   //本地文件
	private static File netFile;     //升级路径上的网络文件
	private String netFilePath;     //升级文件路径
	private JButton messageAlertButton;  //公告消息按钮
	private Stack<String> messageStack;  //公告消息栈
	private ImageIcon messageAlertIcon;   //公告信息图标
	private ImageIcon messageAlertNullIcon;  //公告信息空图标
	private Rectangle location;  //窗体位置
	public static TrayIcon trayicon;  //系统托盘
	private Dao dao;
	/**
	 * systemRoot()得到以注册表路径HKEY_LOCAL_MACHINE\SOFTWARE\Javasoft\Prefs
	 *  为根结点的Preferences对象
	 *  建立一个注册表项目
	 */
	public final static Preferences preferences = Preferences.systemRoot();  //首选项
	
	private JButton userInfoButton;   //本地用户按钮
	public static void main(String args[]) {
		try {
			String laf = preferences.get("lookAndFeel", "java默认");   //获取用户选择的外观
			if (laf.indexOf("当前系统")>-1)     //为啥>-1??
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());       //设置外观
			EQ frame = new EQ();          //创建主窗体对象
			frame.setVisible(true);      //显示窗体
			frame.SystemTrayInitial();  // 初始化系统栏
			frame.server();            //启动服务端口
			frame.checkPlacard();     //检测系统公告
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public EQ() {
		super(new Frame());             //初始化窗体对象
		frame = this;
		dao = Dao.getDao();
		location = dao.getLocation();   //初始化窗体位置对象
		setTitle("EQ通讯");    
		setBounds(location);            //设置窗体位置
		progressBar = new JProgressBar();   //初始化用户搜索进度条
		progressBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		tabbedPane = new JTabbedPane();   //初始化选项卡
		popupMenu = new JPopupMenu();     //初始化弹出菜单
		chatTree = new ChatTree(this);      
		user_dir = System.getProperty("user.dir"); // 程序执行路径用于系统更新，用户当前路径EQ
		localFile = new File(user_dir + File.separator + "EQ.jar");// 本地EQ文件
		stateLabel = new JLabel(); // 状态栏标签
		addWindowListener(new FrameWindowListener());// 添加窗体监视器
		addComponentListener(new ComponentAdapter() {
			public void componentResized(final ComponentEvent e) {   //当窗体改变大小时候
				saveLocation();
			}
			public void componentMoved(final ComponentEvent e) {
				//当窗体位置改变的时候
				saveLocation();
			}
		});
		try {// 启动通讯服务端口
			ss = new DatagramSocket(1111);   //初始化服务器
		} catch (SocketException e2) {
			if (e2.getMessage().startsWith("Address already in use"))
				showMessageDialog("服务端口被占用,或者本软件已经运行。");  //弹出提示框告诉
			System.exit(0);
		}
		{ // 初始化公共信息按钮
			//其实消息通知就是一个gif呀
			messageAlertIcon = new ImageIcon(EQ.class
					.getResource("/image/messageAlert.gif"));  //初始化公告信息按钮
			messageAlertNullIcon = new ImageIcon(EQ.class
					.getResource("/image/messageAlertNull20.gif"));
			messageStack = new Stack<String>();
			messageAlertButton = new JButton();
			messageAlertButton.setHorizontalAlignment(SwingConstants.RIGHT);//小喇叭位置
			messageAlertButton.setContentAreaFilled(false);
			final JPanel BannerPanel = new JPanel();
			BannerPanel.setLayout(new BorderLayout());
			add(BannerPanel, BorderLayout.NORTH);
			userInfoButton = new JButton();
			BannerPanel.add(userInfoButton, BorderLayout.WEST); //本机最上面排头像位置
			userInfoButton.setMargin(new Insets(0, 0, 0, 10));
			initUserInfoButton();// 初始化本地用户头像按钮
			BannerPanel.add(messageAlertButton, BorderLayout.CENTER);
			messageAlertButton.addActionListener(new ActionListener() {   //添加公告信息按钮监听器
				public void actionPerformed(final ActionEvent e) {
					if (!messageStack.empty()) {
						showMessageDialog(messageStack.pop());    //显示公告信息
					}
				}
			});
			messageAlertButton.setIcon(messageAlertIcon);
			showMessageBar();
		}
		/**
		 * 这就是界面的左边的选项
		 * jtabbedpane就是为了使得在不同的选项之间能快速切换
		 * 这里可以不断调节使得自己看起来很舒服
		 */
		add(tabbedPane, BorderLayout.CENTER);   //初始化选项卡面板在界面布局
		tabbedPane.setTabPlacement(SwingConstants.LEFT);  //设置切换卡的位置
		ImageIcon userTicon = new ImageIcon(EQ.class
				.getResource("/image/tabIcon/tabLeft.PNG"));  //创建用户列表图标
		tabbedPane.addTab(null, userTicon, createUserList(), "用户列表");
		ImageIcon sysOTicon = new ImageIcon(EQ.class       //创建系统操作图标
				.getResource("/image/tabIcon/tabLeft2.PNG"));
		tabbedPane.addTab(null, sysOTicon, createSysToolPanel(), "系统操作");   //添加系统操作选项卡
		ImageIcon sysSTicon = new ImageIcon(EQ.class
				.getResource("/image/tabIcon/tabLeft3.png"));   //创建系统设置图标
		tabbedPane.addTab(null, sysSTicon, createSysSetPanel(), "系统设置");  //添加系统设置选项卡
		setAlwaysOnTop(true);   //窗体显示在最顶端
	}

	private JScrollPane createSysSetPanel() {
		final JPanel sysSetPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(sysSetPanel);
		sysSetPanel.setLayout(new BoxLayout(sysSetPanel, BoxLayout.Y_AXIS));
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		final JPanel sysPathPanel = new JPanel();
		sysPathPanel.setMaximumSize(new Dimension(600, 200));
		sysPathPanel.setBorder(new TitledBorder("系统路径"));
		sysPathPanel.setLayout(new GridLayout(0, 1));
		sysSetPanel.add(sysPathPanel);
		sysPathPanel.add(new JLabel("程序升级路径："));
		updatePathTField = new JTextField(preferences
				.get("updatePath", "请输入路径"));
		sysPathPanel.add(updatePathTField);
		sysPathPanel.add(new JLabel("系统公告路径："));
		placardPathTField = new JTextField(preferences.get("placardPath",
				"请输入路径"));
		sysPathPanel.add(placardPathTField);
		sysPathPanel.add(new JLabel("公共程序路径："));
		pubPathTField = new JTextField(preferences.get("pubPath", "请输入路径"));
		sysPathPanel.add(pubPathTField);
		final JButton pathOKButton = new JButton("确定");
		pathOKButton.setActionCommand("sysOK");
		pathOKButton.addActionListener(new SysSetPanelOKListener());
		sysSetPanel.add(pathOKButton);
		final JPanel loginPanel = new JPanel();
		loginPanel.setMaximumSize(new Dimension(600, 90));
		loginPanel.setBorder(new TitledBorder("登录升级服务器"));
		final GridLayout gridLayout_1 = new GridLayout(0, 1);
		gridLayout_1.setVgap(5);
		loginPanel.setLayout(gridLayout_1);
		sysSetPanel.add(loginPanel);
		final JPanel panel_7 = new JPanel();
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));
		loginPanel.add(panel_7);
		panel_7.add(new JLabel("用户名："));
		userNameTField = new JTextField(preferences.get("username", "请输入用户名"));
		panel_7.add(userNameTField);
		final JPanel panel_8 = new JPanel();
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));
		loginPanel.add(panel_8);
		panel_8.add(new JLabel("密　码："));
		passwordTField = new JPasswordField("*****");
		panel_8.add(passwordTField);
		final JButton loginOKButton = new JButton("确定");
		sysSetPanel.add(loginOKButton);
		loginOKButton.setActionCommand("loginOK");
		loginOKButton.addActionListener(new SysSetPanelOKListener());
		final JPanel ipPanel = new JPanel();
		final GridLayout gridLayout_2 = new GridLayout(0, 1);
		gridLayout_2.setVgap(5);
		ipPanel.setLayout(gridLayout_2);
		ipPanel.setMaximumSize(new Dimension(600, 90));
		ipPanel.setBorder(new TitledBorder("IP搜索范围"));
		sysSetPanel.add(ipPanel);
		final JPanel panel_5 = new JPanel();
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		ipPanel.add(panel_5);
		panel_5.add(new JLabel("起始IP："));
		ipStartTField = new JTextField(preferences
				.get("ipStart", "192.168.0.1"));
		panel_5.add(ipStartTField);
		final JPanel panel_6 = new JPanel();
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		ipPanel.add(panel_6);
		panel_6.add(new JLabel("终止IP："));
		ipEndTField = new JTextField(preferences.get("ipEnd", "192.168.1.255"));
		panel_6.add(ipEndTField);
		final JButton ipOKButton = new JButton("确定");
		ipOKButton.setActionCommand("ipOK");
		ipOKButton.addActionListener(new SysSetPanelOKListener());
		sysSetPanel.add(ipOKButton);
		return scrollPane;
	}
/**
 * 用户列表面板,显示chattree
 */
	private JScrollPane createUserList() {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);//水平滚动条从来不显示在
		addUserPopup(chatTree, getPopupMenu());// 为用户添加弹出菜单
		scrollPane.setViewportView(chatTree);  //设置view里面显示的内容
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));   //边框
		chatTree.addMouseListener(new ChatTreeMouseListener());
		return scrollPane;
	}
/**
 * 系统选项卡的面板
 */
	private JScrollPane createSysToolPanel() {// 系统工具面板
		JPanel sysToolPanel = new JPanel(); // 系统工具面板
		sysToolPanel.setLayout(new BorderLayout());
		JScrollPane sysToolScrollPanel = new JScrollPane();
		sysToolScrollPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sysToolScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		sysToolScrollPanel.setViewportView(sysToolPanel);
		sysToolPanel.setBorder(new BevelBorder(BevelBorder.LOWERED)); //设置面板里面的内容是突出显示还是凹显示
		JPanel interfacePanel = new JPanel();
		sysToolPanel.add(interfacePanel, BorderLayout.NORTH);
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.setBorder(new TitledBorder("界面选择-再次启动生效"));
		faceList = new JList(new String[]{"当前系统", "java默认"});
		interfacePanel.add(faceList);
		faceList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		final JPanel interfaceSubPanel = new JPanel();
		interfaceSubPanel.setLayout(new FlowLayout());
		interfacePanel.add(interfaceSubPanel, BorderLayout.SOUTH);
		selectInterfaceOKButton = new JButton("确定");
		selectInterfaceOKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preferences.put("lookAndFeel", faceList.getSelectedValue()
						.toString());
				JOptionPane.showMessageDialog(EQ.this, "重新运行本软件后生效");
			}
		});
		interfaceSubPanel.add(selectInterfaceOKButton);

		JPanel searchUserPanel = new JPanel(); // 用户搜索面板
		sysToolPanel.add(searchUserPanel);
		searchUserPanel.setLayout(new BorderLayout());
		final JPanel searchControlPanel = new JPanel();
		searchControlPanel.setLayout(new GridLayout(0, 1));
		searchUserPanel.add(searchControlPanel, BorderLayout.SOUTH);
		final JList searchUserList = new JList(new String[]{"检测用户列表"});// 新添加用户列表
		final JScrollPane scrollPane_2 = new JScrollPane(searchUserList);
		scrollPane_2.setDoubleBuffered(true);
		searchUserPanel.add(scrollPane_2);
		searchUserList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		searchUserButton = new JToggleButton();
		searchUserButton.setText("搜索新用户");
		searchUserButton.addActionListener(new SearchUserActionListener(searchUserList));
		searchControlPanel.add(progressBar);
		searchControlPanel.add(searchUserButton);
		searchUserPanel.setBorder(new TitledBorder("搜索用户"));

		final JPanel sysUpdatePanel = new JPanel();
		sysUpdatePanel.setOpaque(false);
		sysUpdatePanel.setLayout(new GridBagLayout());
		sysUpdatePanel.setBorder(new TitledBorder("系统操作"));
		sysToolPanel.add(sysUpdatePanel, BorderLayout.SOUTH);
		final JButton sysUpdateButton = new JButton("系统更新");
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridx = 0;
		gridBagConstraints_1.gridy = 0;
		sysUpdatePanel.add(sysUpdateButton, gridBagConstraints_1);
		sysUpdateButton.addActionListener(new SysUpdateListener());// 添加系统更新事件
		final JLabel updateLabel = new JLabel("最近更新：");
		final GridBagConstraints updateLabelLayout = new GridBagConstraints();
		updateLabelLayout.gridy = 1;
		updateLabelLayout.gridx = 0;
		sysUpdatePanel.add(updateLabel, updateLabelLayout);
		final JLabel updateDateLabel = new JLabel();// 程序更新日期标签
		Date date = new Date(localFile.lastModified());
		String dateStr = String.format("%tF %<tr", date);
		updateDateLabel.setText(dateStr);
		final GridBagConstraints updateDateLayout = new GridBagConstraints();
		updateDateLayout.gridy = 2;
		updateDateLayout.gridx = 0;
		sysUpdatePanel.add(updateDateLabel, updateDateLayout);
		final JLabel updateStaticLabel = new JLabel("更新状态：");
		final GridBagConstraints updateStaticLayout = new GridBagConstraints();
		updateStaticLayout.gridy = 3;
		updateStaticLayout.gridx = 0;
		sysUpdatePanel.add(updateStaticLabel, updateStaticLayout);
		final JLabel updateInfoLabel = new JLabel();// 版本信息标签
		checkSysInfo(updateInfoLabel);// 调用检测版本更新的方法
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 4;
		gridBagConstraints_5.gridx = 0;
		sysUpdatePanel.add(updateInfoLabel, gridBagConstraints_5);
		JPanel statePanel = new JPanel();
		add(statePanel, BorderLayout.SOUTH);
		statePanel.setLayout(new BorderLayout());
		statePanel.add(stateLabel);
		stateLabel.setText("总人数：" + chatTree.getRowCount());
		return sysToolScrollPanel;
	}

	/***
	 * 初始化本机用户信息按钮
	 */
	private void initUserInfoButton() {// 初始化用户信息按钮
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();  //获取本地IP
			User user = dao.getUser(ip);   
			userInfoButton.setIcon(user.getIconImg());
			userInfoButton.setText(user.getName());  //显示本机名
			userInfoButton.setIconTextGap(JLabel.RIGHT); //设置文本显示在头像右侧
			userInfoButton.setToolTipText(user.getTipText());   //设置提示文本，鼠标放在本机头像上能有提示IP
			userInfoButton.getParent().doLayout();   
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}
	/**
	 * 显示公告信息按钮的线程,这是一段很经典的代码随时刷新
	 * 其实就是在不断的变换两个图像
	 */
	private void showMessageBar() { 
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (!messageStack.empty()) {
						try {
							messageAlertButton.setIcon(messageAlertNullIcon);
							messageAlertButton.setPreferredSize(new Dimension(
									20, 20));
							Thread.sleep(500);  //睡眠500毫秒之后才换
							messageAlertButton.setIcon(messageAlertIcon);
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
/**
 * 检测版本的更新
 * 还能html显示，吊呀
 * 线程来做的
 */
	private void checkSysInfo(final JLabel updateInfo) {
		new Thread(new Runnable() {
			public void run() {
				String info = "";
				while (true) {
					try {
						netFilePath = preferences.get("updatePath", "EQ.jar");
						if (netFilePath.equals("EQ.jar")) {
							info = "<html><center><font color=red><b>无法登录</b><br>未设置升级路径</font></center></html>";
							updateInfo.setText(info);
							continue;
						}
						netFile = new File(netFilePath);
						if (netFile.exists()) {
							Date netDate = new Date(netFile.lastModified());
							if (!localFile.exists())
								info = "<html><font color=blue>本地程序位置出错！</font></html>";
							else {
								Date localDate = new Date(localFile
										.lastModified());
								if (netDate.after(localDate)) {
									info = "<html><font color=blue>网络上有最新程序！</font></html>";
									pushMessage(info);
								} else
									info = "<html><font color=green>现在是最新程序！</font></html>";
							}
						} else {
							info = "<html><center><font color=red><b>无法访问</b><br>升级路径</font></center></html>";
						}
						updateInfo.setText(info);
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	class SearchUserActionListener implements ActionListener {
		private final JList list;
		SearchUserActionListener(JList list) {
			this.list = list;
		}
		public void actionPerformed(ActionEvent e) {
			if (searchUserButton.isSelected()) {
				searchUserButton.setText("停止搜索");
				new Thread(new Runnable() {
					public void run() {
						Resource.searchUsers(chatTree, progressBar,
								list, searchUserButton);
					}
				}).start();
			} else
				searchUserButton.setText("搜索新用户");
		}
	}

	class SysSetPanelOKListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("sysOK")) {
				String updatePath = updatePathTField.getText();
				String placardPath = placardPathTField.getText();
				String pubPath = pubPathTField.getText();
				preferences.put("updatePath", updatePath); // 设置系统升级路径
				preferences.put("placardPath", placardPath);// 设置系统公告路径
				preferences.put("pubPath", pubPath); // 设置公共程序路径
				JOptionPane.showMessageDialog(EQ.this, "系统设置保存完毕");
			}
			if (command.equals("loginOK")) {
				String username = userNameTField.getText();
				String password = new String(passwordTField.getPassword());
				preferences.put("username", username); // 设置系统升级路径
				preferences.put("password", password);// 设置系统公告路径
				JOptionPane.showMessageDialog(EQ.this, "登录设置保存完毕");
			}
			if (command.equals("ipOK")) {
				String ipStart = ipStartTField.getText();
				String ipEnd = ipEndTField.getText();
				try {
					InetAddress.getByName(ipStart);
					InetAddress.getByName(ipEnd);
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(EQ.this, "IP地址格式错误");
					return;
				}
				preferences.put("ipStart", ipStart); // 设置系统升级路径
				preferences.put("ipEnd", ipEnd);// 设置系统公告路径
				JOptionPane.showMessageDialog(EQ.this, "IP设置保存完毕");
			}
		}
	}
/**
 * 系统监听事件的监听
 */
	private final class SysUpdateListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String username = preferences.get("username", null);
			String password = preferences.get("password", null);
			if (username == null || password == null) {
				pushMessage("未设置登录升级服务器的用户名或密码");
				return;
			}
			Resource.loginPublic(username, password);  //将远程程序映射到本地，但是映射速度以及相关的一些还是不了解
			updateProject();
		}
	}

	private class ChatTreeMouseListener extends MouseAdapter { // 用户列表的监听器
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 2) {
				TreePath path = chatTree.getSelectionPath();
				if (path == null)
					return;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				User user = (User) node.getUserObject();
				try {
					TelFrame.getInstance(ss, new DatagramPacket(new byte[0], 0,
							InetAddress.getByName(user.getIp()), 1111),
							chatTree);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void server() {// 服务器启动方法
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (ss != null) {
						byte[] buf = new byte[4096];
						DatagramPacket dp = new DatagramPacket(buf, buf.length);
						try {
							ss.receive(dp);
						} catch (IOException e) {
							e.printStackTrace();
						}
						TelFrame.getInstance(ss, dp, chatTree);
					}
				}
			}
		}).start();
	}
/**
 * 添加用户弹出菜单
 */
	private void addUserPopup(Component component, final JPopupMenu popup) {// 添加用户弹出菜单
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}
/**
 * 本函数就是在点击用户时候弹出来的菜单选项
 * 显示某些是可以使用和某些功能不可以使用
 */
			private void showMenu(MouseEvent e) {
				if (chatTree.getSelectionPaths() == null) {
					popupMenu.getComponent(0).setEnabled(false);
					popupMenu.getComponent(2).setEnabled(false);
					popupMenu.getComponent(3).setEnabled(false);
					popupMenu.getComponent(4).setEnabled(false);
					popupMenu.getComponent(5).setEnabled(false);
				} else {
					if (chatTree.getSelectionPaths().length < 2) {
						popupMenu.getComponent(3).setEnabled(false);
					} else {
						popupMenu.getComponent(3).setEnabled(true);
					}
					popupMenu.getComponent(0).setEnabled(true);
					popupMenu.getComponent(2).setEnabled(true);
					popupMenu.getComponent(4).setEnabled(true);
					popupMenu.getComponent(5).setEnabled(true);
				}
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	/**
	 * 存储当前窗体外观在数据库里面
	 */
	private void saveLocation() { // 保存主窗体位置的方法
		location = getBounds();  //获取窗体位置和大小
		dao.updateLocation(location);   //调用updateLocation方法
	}
	
	/**
	 * 创建用户弹出菜单，并添加相应的监听函数
	 * 去执行相应的操作
	 */
	protected JPopupMenu getPopupMenu() {// 创建用户弹出菜单
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.setOpaque(false);
		}
		final JMenuItem rename = new JMenuItem();
		popupMenu.add(rename);
		rename.addActionListener(new RenameActionListener());
		rename.setText("重命名");
		final JMenuItem addUser = new JMenuItem();
		addUser.addActionListener(new AddUserActionListener());
		popupMenu.add(addUser);
		addUser.setText("添加用户");
		final JMenuItem delUser = new JMenuItem();
		delUser.addActionListener(new delUserActionListener());
		popupMenu.add(delUser);
		delUser.setText("删除用户");
		final JMenuItem messagerGroupSend = new JMenuItem();
		messagerGroupSend
				.addActionListener(new messagerGroupSendActionListener());
		messagerGroupSend.setText("消息群发");
		popupMenu.add(messagerGroupSend);
		final JMenuItem accessComputerFolder = new JMenuItem("访问资源");
		accessComputerFolder.setActionCommand("computer");
		popupMenu.add(accessComputerFolder);
		accessComputerFolder
				.addActionListener(new accessFolderActionListener());
		final JMenuItem accessPublicFolder = new JMenuItem();
		popupMenu.add(accessPublicFolder);
		accessPublicFolder.setOpaque(false);
		accessPublicFolder.setText("访问公共程序");
		accessPublicFolder.setActionCommand("public");
		accessPublicFolder.addActionListener(new accessFolderActionListener());
		return popupMenu;
	}
	/**
	 * 更新程序版本
	 * 思路就是:比较程序时间很服务器版本最后修改的时间
	 * 开辟线程来更新
	 */
	private void updateProject() { 
		netFilePath = preferences.get("updatePath", "EQ.jar");
		if (netFilePath.equals("EQ.jar")) {  //如果首选项没有升级路径
			pushMessage("未设置升级路径");
			return;
		}
		netFile = new File(netFilePath);  //创建服务器服务对象，这是远程文件映射到updatePath的文件
		localFile = new File(user_dir + File.separator + "EQ.jar");  //创建本地文件对象
		if (localFile != null && netFile != null && netFile.exists()
				&& localFile.exists()) {
			Date netDate = new Date(netFile.lastModified());  //文件最后修改的时间
			Date localDate = new Date(localFile.lastModified());
			if (netDate.after(localDate)) {
				new Thread(new Runnable() {  //Thread 线程类
											 //runnable  线程接口
					public void run() {  //线程中的方法
						try {
							Dialog frameUpdate = new UpdateFrame();
							frameUpdate.setVisible(true);
							Thread.sleep(2000);
							
							/**
							 * 一段升级程序常用方法
							 */
							FileInputStream fis = new FileInputStream(netFile);
							FileOutputStream fout = new FileOutputStream(
									localFile);
							int len = fis.available();
							if (len > 0) {
								byte[] data = new byte[len];
								if (fis.read(data) > 0) {
									fout.write(data);
								}
							}
							fis.close();
							fout.close();
							frameUpdate.setVisible(false);
							frameUpdate = null;
							showMessageDialog("更新完毕，请重新启动程序。");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();  //开始执行线程
			} else {
				showMessageDialog("已经是最新的程序了。");
			}
		}
	}
     /**
      *检测公告信息 
      */
	private void checkPlacard() { 
		String placardDir = preferences.get("placardPath", null);  //获取公告路径
		if (placardDir == null) {
			pushMessage("未设置公告路径，请去系统设置设置！");
			return;
		}
		File placard = new File(placardDir);   //创建公告文件对象
		try {
			if (placard.exists() && placard.isFile()) {
				StringBuilder placardStr = new StringBuilder();
				Scanner sc = new Scanner(new FileInputStream(placard));
				while (sc.hasNextLine()) {   //读取文件公告内容
					placardStr.append(sc.nextLine());
				}
				pushMessage(placardStr.toString());
			}
		} catch (FileNotFoundException e) {
			pushMessage("公告路径错误，或公告文件不存在");  
		}
	}
 
	/**
	 *  设置状态栏信息
	 */
	public void setStatic(String str) {
		if (stateLabel != null)
			stateLabel.setText(str);
	}
/**
 * 将相应的消息压入，线程会时刻去检测，达到输出的目的
 */
	private void pushMessage(String info) {
		if (!messageStack.contains(info))
			messageStack.push(info);
	}
/**
 * 显示信息的方法
 */
	private void showMessageDialog(String mess) {
		JOptionPane.showMessageDialog(this, mess);
	}

	/**
	 * 显示输入对话框
	 */
	private String showInputDialog(String str) { 
		String newName = JOptionPane.showInputDialog(this,
				"<html>输入<font color=red><strong>" + str + "</strong></font>的新名字</html>");
		return newName;
	}
/**
 * 访问资源监听
 */
	private class accessFolderActionListener implements ActionListener {// 访问资源
		public void actionPerformed(final ActionEvent e) {
			
			System.out.println("现在执行的是访问主机资源");
			
			TreePath path = chatTree.getSelectionPath();
			if (path == null)
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			User user = (User) node.getUserObject();
			String ip = "\\\\"+user.getIp();
			String command = e.getActionCommand();   //为了区分访问computer和public时候设置的
			if (command.equals("computer")) {
				Resource.startFolder(ip);   //访问主机资源
			}
			
			if (command.equals("public")) {
				String serverPaeh = preferences.get("pubPath", null);
				if (serverPaeh == null) {
					pushMessage("未设置公共程序路径");  //访问公共程序
					return;
				}
				Resource.startFolder(serverPaeh);
			}
		}
	}
	
/**
 * 更名监听事件
 * 获得节点path,取得用户name
 * 重写，重载人
 */
	private class RenameActionListener implements ActionListener {// 更名
		public void actionPerformed(final ActionEvent e) {
			TreePath path = chatTree.getSelectionPath();
			if (path == null)
				return;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			User user = (User) node.getUserObject();
			String newName = showInputDialog(user.getName());
			if (newName != null && !newName.isEmpty()) {
				user.setName(newName);
				dao.updateUser(user);
				DefaultTreeModel model = (DefaultTreeModel) chatTree.getModel();
				model.reload();   //修改之后再次重新载入
				chatTree.setSelectionPath(path);
				initUserInfoButton();  //不理解为什么还要初始化一次
			}
		}
	}
/**
 * 关闭windows其实就是设置它不可见，在后台运行，然后点解系统托盘的"打开"再次设置为可见
 * 
 */
	private class FrameWindowListener extends WindowAdapter {
		public void windowClosing(final WindowEvent e) {  //windowsAdapter是抽象类
			setVisible(false);
		}
	}
/**
 * 右键添加用户的监听程序，调用chatTree.add()函数实现好友的添加
 * @author ldz
 *
 */
	private class AddUserActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {// 添加用户
			String ip = JOptionPane.showInputDialog(EQ.this, "输入新用户IP地址");
			if (ip != null)
				chatTree.addUser(ip, "add");
		}
	}
	/**
	 * 删除监听事件
	 * @author ldz
	 *
	 */
	private class delUserActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {// 删除用户
			chatTree.delUser();
		}
	}
	/**
	 * 消息群发
	 * @author ldz
	 *
	 */
	private class messagerGroupSendActionListener implements ActionListener {// 信使群发
		public void actionPerformed(final ActionEvent e) {
			String message = JOptionPane.showInputDialog(EQ.this, "请输入群发信息",
					"信使群发", JOptionPane.INFORMATION_MESSAGE);
			if (message != null && !message.equals("")) {
				TreePath[] selectionPaths = chatTree.getSelectionPaths();
				Resource.sendGroupMessenger(selectionPaths, message);
			} else if (message != null && message.isEmpty()) {
				JOptionPane.showMessageDialog(EQ.this, "不能发送空信息！");
			}
		}
	}
	/**
	 * 初始化系统托盘
	 */
	private void SystemTrayInitial() { 
		if (!SystemTray.isSupported()) // 判断当前系统是否支持系统栏
			return;
		try {
			String title = "EQ";
			String company = "donahue";
			SystemTray sysTray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(
					EQ.class.getResource("/icons/sysTray1.png"));// 系统栏图标，更换
			trayicon = new TrayIcon(image, title + "\n" + company, createMenu());
			trayicon.setImageAutoSize(true);
			trayicon.addActionListener(new SysTrayActionListener());
			sysTray.add(trayicon);
			trayicon.displayMessage(title, company, MessageType.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private PopupMenu createMenu() { // 创建系统栏菜单的方法
		PopupMenu menu = new PopupMenu();
		MenuItem exitItem = new MenuItem("退出");
		exitItem.addActionListener(new ActionListener() { // 系统栏退出事件
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
		MenuItem openItem = new MenuItem("打开");
		openItem.addActionListener(new ActionListener() {// 系统栏打开菜单项事件
					public void actionPerformed(ActionEvent e) {
						if (!isVisible()) {
							setVisible(true);
							toFront();
						} else
							toFront();
					}
				});

		// 系统栏的访问服务器菜单项事件
		MenuItem publicItem = new MenuItem("访问服务器");
		publicItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String serverPaeh = preferences.get("pubPath", null);
				if (serverPaeh == null) {
					pushMessage("未设置公共程序路径");
					return;
				}
				Resource.startFolder(serverPaeh);
			}
		});
		menu.add(publicItem);
		menu.add(openItem);
		menu.addSeparator();
		menu.add(exitItem);
		return menu;
	}
	class SysTrayActionListener implements ActionListener {// 系统栏双击事件
		public void actionPerformed(ActionEvent e) {
			setVisible(true);
			toFront();
		}
	}
}