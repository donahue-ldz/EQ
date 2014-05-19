package com.lzw.userList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import com.lzw.EQ;
import com.lzw.dao.Dao;
public class UserTreeRanderer extends JPanel implements TreeCellRenderer {
	private Icon openIcon, closedIcon, leafIcon; //节点图标
	private String tipText = "";
	private final JCheckBox label = new JCheckBox();  //用户选择图标
	private final JLabel headImg = new JLabel();   //用户头像
	private static User user;    //用户对象
	public UserTreeRanderer() {
		super();
		user = null;
	}
	public UserTreeRanderer(Icon open, Icon closed, Icon leaf) {
		openIcon = open;      //节点展开图标
		closedIcon = closed;   //节点关闭图标
		leafIcon = leaf;       //叶节点图标
		setBackground(new Color(0xF5B9BF));   //设置背景色
		label.setFont(new Font("宋体", Font.BOLD, 14));  //设置字体
		URL trueUrl = EQ.class                   //选择用户图标
				.getResource("/image/chexkBoxImg/CheckBoxTrue.png");
		label.setSelectedIcon(new ImageIcon(trueUrl));
		URL falseUrl = EQ.class      //取消用户图标
				.getResource("/image/chexkBoxImg/CheckBoxFalse.png");
		label.setIcon(new ImageIcon(falseUrl));
		label.setForeground(new Color(0, 64, 128));
		final BorderLayout borderLayout = new BorderLayout();   //创建布局
		setLayout(borderLayout);
		user = null;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 * 重写父节点方法
	 * 负责渲染树节点样式
	 * 获取主体宽度
	 * 选择节点时候刚方法使用指定颜色设置节点边框
	 * 
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode) {      //判断value是否是节点
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;   
			Object uo = node.getUserObject();   //获取节点信息
			if (uo instanceof User)           //如果该数据是user类的实例的话
				user = (User) uo;            //初始化user对象
		} else if (value instanceof User)    //如果value是user类的实例
			user = (User) value;   //初始化user对象
		if (user != null && user.getIcon() != null) {
			int width = EQ.frame.getWidth();     //获取主体宽度
			if (width > 0)
				setPreferredSize(new Dimension(width, user.getIconImg()    //让节点和窗体同宽
						.getIconHeight()));
			headImg.setIcon(user.getIconImg());   //头像的设置
			tipText = user.getName();
		} else {
			if (expanded)
				headImg.setIcon(openIcon);
			else if (leaf)
				headImg.setIcon(leafIcon);
			else
				headImg.setIcon(closedIcon);

		}
		add(headImg, BorderLayout.WEST);   //添加用户头像
		label.setText(value.toString());   //设置用户名
		label.setOpaque(false);
		add(label, BorderLayout.CENTER);         //添加用户名称
		if (selected) {   //如果该节点呗选择
			label.setSelected(true);
			setBorder(new LineBorder(new Color(0xD46D73), 2, false));  //以指定颜色边框绘制
			setOpaque(true);
		} else {
			setOpaque(false);
			label.setSelected(false);
			setBorder(new LineBorder(new Color(0xD46D73), 0, false));   //否则回复原来的颜色
		}
		return this;
	}
	public String getToolTipText() {
		return tipText;
	}
}
