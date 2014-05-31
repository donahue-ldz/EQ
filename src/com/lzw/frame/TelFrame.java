package com.lzw.frame;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.*;
import com.lzw.EQ;
import com.lzw.dao.Dao;
import com.lzw.system.Resource;
import com.lzw.userList.ChatTree;
import com.lzw.userList.User;
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
/**
 * 这是聊天时候的界面
 * @author 德钊
 *
 */
public class TelFrame extends JFrame {
	private Dao dao;
	private User user;
	private JTextPane receiveText = new JTextPane();  //信息接收文本框
	private JScrollPane scrollPane = new JScrollPane();  //消息显示上半，分割的上半部分
	private JTextPane sendText = new JTextPane();  //信息编辑文本框
	private JScrollPane scrollPane_1 = new JScrollPane();  //容纳发送消息的编写面板
	private JSplitPane splitPane = new JSplitPane();
	private JButton sendButton = new JButton();
	private final JButton messageButton = new JButton();
	private JPanel panel = new JPanel();  //包含“发送”和“信使”按钮的面板
	private final static Map<String, TelFrame> instance = new HashMap<String, TelFrame>();
	private final JCheckBox messageMode = new JCheckBox();  //消息模式复选框
	private JToolBar toolBar = new JToolBar();     //工具条
	private JToggleButton toolFontButton = new JToggleButton();  //字体按钮，按下之后不弹起
	private JButton toolFaceButton = new JButton();  //表情按钮
	private JButton button = new JButton();
	private JButton button_3 = new JButton();  //聊天场景选择按钮
	private final JButton button_1 = new JButton();  //将右面窗口合并的<
	private final JPanel panel_5 = new JPanel();  //包含所有工具的面板
	private JPanel panel_2 = new JPanel();  //splite分割的下半部分，就是你消息编辑所在的面板
	private JPanel panel_1 = new JPanel();
	private JLabel label = new JLabel();  //显示的是用户IP等详细信息
	private final JScrollPane scrollPane_2 = new JScrollPane();
	private final JLabel label_1 = new JLabel();  //用户形象
	private JPanel panel_3 = new JPanel();  //右侧显示用户信息的面板
	private byte[] buf;   //缓冲
	private DatagramSocket ss;
	private String ip;
	private DatagramPacket dp;
	private TelFrame frame;
	private ChatTree tree;
	private int rightPanelWidth = 148;  //右侧面板宽度
	/**
	 * 获取窗体实例，该方法创建的所有实例都会保存在map集合类实例中，除非退出，否则一直保存
	 * 用户再次打开已经存在的窗体时候将直接在map里面读取，不再创建新窗体
	 * @param ssArg
	 * @param dp
	 * @param treeArg
	 * @return
	 */
	public static synchronized TelFrame getInstance(DatagramSocket ssArg,
			DatagramPacket dp, ChatTree treeArg) {
		String tmpIp = dp.getAddress().getHostAddress();//获取数据包IP
		if (!instance.containsKey(tmpIp)) {  //如果集合中不存在该用户窗体 containsKey（）判断集合中有元素
			TelFrame frame = new TelFrame(ssArg, dp, treeArg);  //创建
			instance.put(tmpIp, frame);  //将窗体实例保存在集合中
			frame.receiveInfo(treeArg);   //接收消息
			if (!frame.isVisible()) {  //如果窗体处于隐藏状态则显示
				frame.setVisible(true);
			}
			frame.setState(JFrame.NORMAL);
			frame.toFront();  //将窗体放在最前端
			return frame;
		} else {    //存在窗体实例在集合中
			TelFrame frame = instance.get(tmpIp);
			frame.setBufs(dp.getData());  //不知道为啥要设置相应的buff  上面不存在时候都没设置相应的buff？
			frame.receiveInfo(treeArg);
			if (!frame.isVisible()) {
				frame.setVisible(true);
			}
			frame.setState(JFrame.NORMAL);
			frame.toFront();
			return frame;
		}
	}
/**
 * 初始化窗口框架控件
 * @param ssArg
 * @param dpArg
 * @param treeArg
 */
	public TelFrame(DatagramSocket ssArg, DatagramPacket dpArg,
			final ChatTree treeArg) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.tree = treeArg;
		ip = dpArg.getAddress().getHostAddress();
		dao = Dao.getDao();
		user = dao.getUser(ip);
		frame = this;
		ss = ssArg;
		dp = dpArg;
		buf = dp.getData();
		try {
			setBounds(200, 100, 521, 424);  //设置窗体的位置和大小
			getContentPane().add(splitPane);  //添加分割面板
			splitPane.setDividerSize(2);   //设置面板分割线大小 消息显示和消息编辑上下的分割线
			splitPane.setResizeWeight(0.8);  //简单的理解就是分割窗格上下的比例
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);  //定义面板竖向分割，即上下分割
			splitPane.setLeftComponent(scrollPane);   //设置面板上半部控件是滚动面板
			scrollPane.setViewportView(getReceiveText());  //设定显示区域
			receiveText.setFont(new Font("宋体", Font.PLAIN, 12));
    		receiveText.setInheritsPopupMenu(true); //设置如果此组件没有分配给它的 JPopupMenu，那么 getComponentPopupMenu 是否应该委托给其父组件
			receiveText.setVerifyInputWhenFocusTarget(false);
			receiveText.setDragEnabled(true);  //是否允许拖动模式，就是可以直接将上面文字拖到其他地点
			receiveText.setMargin(new Insets(0, 0, 0, 0));  //设置组件的边框和它的文本之间的空白
			receiveText.setEditable(false);   //使文本框只读
			/**
			 * 
			 * 当组建大小改变之后重绘大小
			 */
			getReceiveText().addComponentListener(new ComponentAdapter() {  //发送按钮监听事件
				public void componentResized(final ComponentEvent e) {  
					scrollPane.getVerticalScrollBar().setValue(
							getReceiveText().getHeight());
				}
			});
			getReceiveText().setDoubleBuffered(true);//设置此组件是否应该使用缓冲区进行绘制
           /**
            * 下半部分布局管理
            */
			splitPane.setRightComponent(panel_2);  //设置分割下半面板
			panel_2.setLayout(new BorderLayout());
            /**
             * flowLayout布局没有用吧
             */
     		final FlowLayout flowLayout = new FlowLayout();
		    flowLayout.setHgap(4);  //设置组件之间以及组件与 Container 的边之间的水平间隙。
			flowLayout.setAlignment(FlowLayout.LEFT);  //设置此布局的对齐方式
			flowLayout.setVgap(0);
			
			
			/**
			 * 发送按钮所对的panel布局
			 */
			panel_2.add(panel, BorderLayout.SOUTH);
			final FlowLayout flowLayout_1 = new FlowLayout();
			flowLayout_1.setVgap(3);
			flowLayout_1.setHgap(20);
			panel.setLayout(flowLayout_1);

			panel.add(sendButton);
			sendButton.setMargin(new Insets(0, 14, 0, 14));  //设置按钮大小
			sendButton.addActionListener(new sendActionListener());
			sendButton.setText("发送");   //设置按钮文本

			panel.add(messageButton);   //添加信使按钮
			messageButton.setMargin(new Insets(0, 14, 0, 14));
			messageButton.addActionListener(new MessageButtonActionListener());  //信使监听
			messageButton.setText("信史");  //信使文本

			panel_2.add(panel_5, BorderLayout.NORTH); //panel_5是包含所有工具的面板
			panel_5.setLayout(new BorderLayout());
			
			toolbarActionListener toolListener = new toolbarActionListener();
			panel_5.add(toolBar);  //面板里面再放一个工具条
			toolBar.setBorder(new BevelBorder(BevelBorder.RAISED));
			toolBar.setFloatable(false);
			toolBar.add(toolFontButton);  //增加字体按钮
			toolFontButton.addActionListener(toolListener);
			toolFontButton.setFocusPainted(false);
			toolFontButton.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarFontIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarFont.png"));
			toolFontButton.setIcon(toolbarFontIcon);
			toolFontButton.setToolTipText("字体颜色和格式");
			toolBar.add(toolFaceButton);
			toolFaceButton.addActionListener(toolListener);
			toolFaceButton.setToolTipText("表情");
			toolFaceButton.setFocusPainted(false);
			toolFaceButton.setMargin(new Insets(0, 0, 0, 0));

			ImageIcon toolbarFaceIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarFace.png"));
			toolFaceButton.setIcon(toolbarFaceIcon);
			toolBar.add(button);
			
			button.addActionListener(toolListener);
			button.setToolTipText("传送文件");
			button.setFocusPainted(false);
			button.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarPictureIcon = new ImageIcon(
					EQ.class
							.getResource("/image/telFrameImage/toolbarImage/ToolbarPicture.png"));
			button.setIcon(toolbarPictureIcon);
			toolBar.add(button_3);
			button_3.addActionListener(toolListener);
			button_3.setToolTipText("选择聊天场景");
			button_3.setFocusPainted(false);
			button_3.setMargin(new Insets(0, 0, 0, 0));
			ImageIcon toolbarSceneIcon = new ImageIcon(
					EQ.class.getResource("/image/telFrameImage/toolbarImage/ToolbarScene.png"));
			button_3.setIcon(toolbarSceneIcon);
			System.currentTimeMillis();
			toolBar.add(messageMode);
			messageMode.setText("消息模式");  //现在还不知道消息模式是做什么的
			panel_5.add(button_1, BorderLayout.EAST);
			button_1.addActionListener(new Button_1ActionListener());
			button_1.setMargin(new Insets(0, 0, 0, 0));
			button_1.setText("<");
			button_1.setToolTipText("合并右边窗口");
			
			/*
			 * 发送消息模块
			 */
			panel_2.add(panel_1);
			panel_1.setLayout(new BorderLayout());
			panel_1.add(scrollPane_1);
			scrollPane_1   //不支持水平滚动
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sendText.setInheritsPopupMenu(true);
			sendText.addKeyListener(new SendTextKeyListener());  //增加对特定键的监听
			sendText.setVerifyInputWhenFocusTarget(false);  //滚动面板一般设置为false
			sendText.setFont(new Font("宋体", Font.PLAIN, 12));  //字体设置
			sendText.setMargin(new Insets(0, 0, 0, 0));
			sendText.setDragEnabled(true);  //允许拖拽
			sendText.requestFocus();
			scrollPane_1.setViewportView(getSendText());

				addWindowListener(new TelFrameClosing(tree));
			add(panel_3, BorderLayout.EAST);
			panel_3.setLayout(new BorderLayout());
			panel_3.add(scrollPane_2);
			scrollPane_2
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			scrollPane_2.setViewportView(label);
			label.setIconTextGap(-1);
			String imgPath = EQ.class
					.getResource("/image/telFrameImage/telUserInfo.png")
					+ "";
			label.setText("<html><body background='" + imgPath
					+ "'><table width='" + rightPanelWidth
					+ "'><tr><td>用户名：<br>&nbsp;&nbsp;" + user.getName()
					+ "</td></tr><tr><td>主机名：<br>&nbsp;&nbsp;" + user.getHost()
					+ "</td></tr>" + "<tr><td>IP地址：<br>&nbsp;&nbsp;" + user.getIp()
					+ "</td></tr><tr><td colspan='2' height="
					+ this.getHeight() * 2
					+ "></td></tr></table></body></html>");  //设置窗体右侧用户消息

			panel_3.add(label_1, BorderLayout.NORTH);
			label_1.setIcon(new ImageIcon(EQ.class
					.getResource("/image/telFrameImage/telUserImage.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setVisible(true);
		setTitle("与『" + user + "』通讯中");  //设置窗体标题
	}
	/**
	 * 接受消息 显示发送者和时间
	 * 在数据包里面获得IP查找用户
	 * @param tree
	 */
	private void receiveInfo(final ChatTree tree) {// 接收信息
		if (buf.length > 0) {
			String rText = new String(buf).replace("" + (char) 0, "");
			String hostAddress = dp.getAddress().getHostAddress();
			String info = dao.getUser(hostAddress).getName();
			info = info + "  (" + new Date().toLocaleString() + ")";
			appendReceiveText(info, Color.red);
			appendReceiveText(rText + "\n", null);
		}
	}
 /**
  * 创建发送按钮事件监听类，他是一个内部类
  * 实现了actonlistener接口  拥有处理按钮事件的能力
  * 思路：将文本消息存在本地一份，用于本地窗口显示
  * 再将消息发送到socket去
  * @author ldz
  *
  */
	class sendActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String sendInfo = getSendInfo();   //获取发送信息
			if (sendInfo == null)
				return;
			insertUserInfoToReceiveText(tree);
			appendReceiveText(sendInfo + "\n", null);    //添加到信息文本框
			byte[] tmpBuf = sendInfo.getBytes();
			DatagramPacket tdp = null;
			try {
				tdp = new DatagramPacket(tmpBuf, tmpBuf.length,
						new InetSocketAddress(ip, 1111));
				ss.send(tdp);
			} catch (SocketException e2) {
				e2.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(TelFrame.this, e1
						.getMessage());
			}
			sendText.setText(null);    //清空发送文本框
			sendText.requestFocus();   //使得发送文本获得焦点
			if (messageMode.isSelected())  //如果选择了消息模式
				setState(ICONIFIED);  //窗体最小化
		}
	}
   /**
    * 待开发功能的显示
    * @author ldz
    *
    */
	class toolbarActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			JOptionPane.showMessageDialog(TelFrame.this, "此功能待开发");
		}
	}
  /**
   * 关闭窗口类
   * @author ldz
   *
   */
	private final class TelFrameClosing extends WindowAdapter {
		private final JTree tree;

		private TelFrameClosing(JTree tree) {
			this.tree = tree;
		}
	
		public void windowClosing(final WindowEvent e) {
			
			TelFrame.this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			int response=JOptionPane
			.showConfirmDialog(TelFrame.this, "你确定关闭", "提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(response==JOptionPane.NO_OPTION)
				return ;
			tree.setSelectionPath(null);
			TelFrame.this.setState(ICONIFIED);  //最小化
			//释放由此 Window、其子组件及其拥有的所有子组件所使用的所有本机屏幕资源
			TelFrame.this.dispose();
		}
	}
  /**
   * 信使按钮监听
   * 当用户输入消息之后，点信使按钮之后将按照信使的方法发送
   * @author ldz
   *
   */
	private class MessageButtonActionListener implements ActionListener {// 信史按钮
		public void actionPerformed(final ActionEvent e) {
			try {
				Document doc = sendText.getDocument();   //获取文档对象
				String sendInfo = doc.getText(0, doc.getLength());  //获取发送的通信信息
				if (sendInfo.equals("") || sendInfo == null) {
					JOptionPane.showMessageDialog(TelFrame.this, "不能发送空信息。");
					return;
				}
				insertUserInfoToReceiveText(tree);
				appendReceiveText(sendInfo, null);   
				Resource.sendMessenger(user, sendInfo, frame);
				sendText.setText(null);//清空发送文本框
				sendText.requestFocus();  //使得文本框获得焦点
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}
	}
	private class SendTextKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.isControlDown() && e.getKeyCode() == 10)
				sendButton.doClick();  //Ctrl+Enter表示发送
			else if (e.isShiftDown() && e.getKeyCode() == 10)
				messageButton.doClick();        //按下shift+Enter之后发的是信使
		}
	}
	private class Button_1ActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			panel_3.setVisible(!panel_3.isVisible());  //经典呀
			TelFrame.this.setVisible(true);
		}
	}

	public JButton getSendButton() {
		return sendButton;
	}

	public JTextPane getReceiveText() {
		return receiveText;
	}

	public void setBufs(byte[] bufs) {
		this.buf = bufs;
	}
    /**
     * getDoucument.gettext()获得输入面板的内容
     * 进行空消息判断
     * @return
     */
	public String getSendInfo() {
		String sendInfo = "";
		Document doc = sendText.getDocument();
		try {
			sendInfo = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		if (sendInfo.equals("")) {
			JOptionPane.showMessageDialog(TelFrame.this, "不能发送空信息。");
			return null;
		}
		return sendInfo;
	}
  /**
   * 将发件人的信息加入到数据报里面，目的是在本地窗口也可见
   * @param tree
   */
	private void insertUserInfoToReceiveText(final ChatTree tree) {
		String info = null;
		try {
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			info = dao.getUser(hostAddress).getName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		info = info + "  (" + new Date().toLocaleString() + ")";
		appendReceiveText(info, new Color(68, 184, 29));  //使自己也能看到自己发送了那些内容
	}
 /**
  * 取得sendtext内容
  * @returnible(false);
  */
	public JTextPane getSendText() {
		return sendText;
	}
   /**
    * 将发送者发送的消息显示在receiveText位置，并设置相应的属性
    * 思路是：先设置属性，然后将属性应用到相应的文字上面 
    * @param sendInfo
    * @param color
    */
	public void appendReceiveText(String sendInfo, Color color) {
		Style style = receiveText.addStyle("title", null); //将一个新样式添加到逻辑样式层次结构中。
		if (color != null) {
			StyleConstants.setForeground(style, color);    //设置前景色
		} else {
			StyleConstants.setForeground(style, Color.BLACK);
		}
		receiveText.setEditable(true);
		//getDocument()获取与编辑器关联的模型
		receiveText.setCaretPosition(receiveText.getDocument().getLength());//设置 TextComponent 的文本插入符的位置
		receiveText.setCharacterAttributes(style, false);  //将给定属性应用于字符内容
		receiveText.replaceSelection(sendInfo + "\n");  //用给定字符串所表示的新内容替换当前选择的内容
		receiveText.setEditable(false);
	}
}
