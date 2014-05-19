package com.lzw.userList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import com.lzw.EQ;
import com.lzw.dao.Dao;
/*
 * 继承jtree类，实现自定义树控件
 * 使用之前定义的树节点渲染器
 * 调用sortUser（）添加并且显示用户列表
 */
public class ChatTree extends JTree {
	private DefaultMutableTreeNode root;
	private DefaultTreeModel treeModel;
	private List<User> userMap;
	private Dao dao;
	private EQ eq;
	public ChatTree(EQ eq) {
		super();
		root = new DefaultMutableTreeNode("root");  //初始化更节点
		treeModel = new DefaultTreeModel(root);
		userMap = new ArrayList<User>();    //初始化用户集合
		dao = Dao.getDao();
		addMouseListener(new ThisMouseListener());
		setRowHeight(50);   //设置节点的高度
		setToggleClickCount(2);
		setRootVisible(false);   //隐藏根节点
		DefaultTreeCellRenderer defaultRanderer = new DefaultTreeCellRenderer();  //创建自定义节点渲染器
		UserTreeRanderer treeRanderer = new UserTreeRanderer(defaultRanderer
				.getOpenIcon(), defaultRanderer.getClosedIcon(),
				defaultRanderer.getLeafIcon());
		setCellRenderer(treeRanderer);   //设置该节点渲染器
		setModel(treeModel);   //添加并显示所有节点
		sortUsers();
		this.eq = eq;
	}
	/*
	 * 主体是一个内部线程
	 * 首先获得本地用户
	 *这个写法感觉好高深呀！！！！
	 */
	private synchronized void sortUsers() {//排序用户列表
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(100);  //线程休眠100秒
					root.removeAllChildren();
					String ip = InetAddress.getLocalHost().getHostAddress();  //获取本地ip
					User localUser = dao.getUser(ip);
					if (localUser != null) {// 把自己显示在首位   
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(
								localUser);
						root.add(node);
					}
					userMap = dao.getUsers();    //获取数据库中所以用户
					Iterator<User> iterator = userMap.iterator();
					while (iterator.hasNext()) { // 从集合中装载用户信息
						User user = iterator.next();
						if(user.getIp().equals(localUser.getIp()))
							continue;
						root.add(new DefaultMutableTreeNode(user));   //添加用户到根节点
					}
					treeModel.reload();
					ChatTree.this.setSelectionRow(0);  //使得第一个节点被选中
					if (eq != null)
						eq.setStatic("　　总人数：" + getRowCount());  //更新状态栏标签
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	/*
	 * 首先获取选择的树节点，选择绑定的对象，然后删除
	 */
	public void delUser() { // 删除用户
		TreePath path = getSelectionPath();
		if (path == null)
			return;
		User user = (User) ((DefaultMutableTreeNode) path
				.getLastPathComponent()).getUserObject();
		int operation = JOptionPane.showConfirmDialog(this, "确定要删除用户：" + user
				+ "?", "删除用户", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (operation == JOptionPane.YES_OPTION) {
			dao.delUser(user);
			root.remove((DefaultMutableTreeNode)path.getLastPathComponent());
			treeModel.reload();
		}
	}
	public boolean addUser(String ip, String opration) {// 添加用户
		try {
			if (ip == null)
				return false;
			User oldUser = dao.getUser(ip);
			if (oldUser == null) {// 如果数据库中不存在该用户
				InetAddress addr = InetAddress.getByName(ip);
				if (addr.isReachable(1500)) {
					String host = addr.getHostName();
					root.add(new DefaultMutableTreeNode(new User(host, ip)));
					User newUser = new User();
					newUser.setIp(ip);
					newUser.setHost(host);
					newUser.setName(host);
					newUser.setIcon("1.gif");  //默认的ICON
					dao.addUser(newUser);
					sortUsers();
					if (!opration.equals("search"))
						JOptionPane.showMessageDialog(EQ.frame, "用户" + host
								+ "添加成功", "添加用户",
								JOptionPane.INFORMATION_MESSAGE);
					return true;
					
				} else {
					if (!opration.equals("search"))
						JOptionPane.showMessageDialog(EQ.frame, "检测不到用户IP："
								+ ip, "错误添加用户", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} else {
				if (!opration.equals("search"))
					JOptionPane.showMessageDialog(EQ.frame, "已经存在用户IP" + ip,
							"不能添加用户", JOptionPane.WARNING_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}
	/*
	 * 鼠标监听事件
	 * 
	 */
	private class ThisMouseListener extends MouseAdapter {//鼠标事件监听器
		public void mousePressed(final MouseEvent e) {
			if (e.getButton() == 3) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (!isPathSelected(path))
					setSelectionPath(path);
			}
		}
	}
}
