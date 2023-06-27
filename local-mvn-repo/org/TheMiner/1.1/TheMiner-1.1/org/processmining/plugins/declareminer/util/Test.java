package org.processmining.plugins.declareminer.util;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class Test {
	static Color red = Color.RED;
	static JPanel jp = new JPanel();
public Test() {	}
public static void main(String[] args) {
	jp.setBackground(red);
	jp.setSize(100, 100);
	jp.addMouseListener(new MouseListener() {
		
		public void mouseClicked(MouseEvent e) {
			red = red.brighter();
			jp.setBackground(red);
			
		}
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	});
	jp.setVisible(true);
	
}

}