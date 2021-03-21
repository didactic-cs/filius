/*
 ** This file is part of Filius, a network construction and simulation software.
 ** 
 ** Originally created at the University of Siegen, Institute "Didactics of
 ** Informatics and E-Learning" by a students' project group:
 **     members (2006-2007): 
 **         AndrÃ© Asschoff, Johannes Bade, Carsten Dittich, Thomas Gerding,
 **         Nadja HaÃŸler, Ernst Johannes Klebert, Michell Weyer
 **     supervisors:
 **         Stefan Freischlad (maintainer until 2009), Peer Stechert
 ** Project is maintained since 2010 by Christian Eibl <filius@c.fameibl.de>
 **         and Stefan Freischlad
 ** Filius is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 2 of the License, or
 ** (at your option) version 3.
 ** 
 ** Filius is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied
 ** warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 ** PURPOSE. See the GNU General Public License for more details.
 ** 
 ** You should have received a copy of the GNU General Public License
 ** along with Filius.  If not, see <http://www.gnu.org/licenses/>.
 */
package filius.gui;

import java.awt.Color;

public class Palette {	
	
	public static final Color DHCP_TABLE_EVEN_ROW_BG               = new Color(250, 248, 246);  // Light warm gray
	public static final Color DHCP_TABLE_ODD_ROW_BG                = new Color(244, 242, 240);  // Slightly darker gray
	
	
	public static final Color ROUTER_TABLE_EVEN_ROW_BG             = new Color(250, 248, 246);  // Light warm gray
	public static final Color ROUTER_TABLE_ODD_ROW_BG              = new Color(244, 242, 240);  // Slightly darker gray
	
	
	public static final Color SAT_TABLE_EVEN_ROW_BG                = new Color(250, 248, 246);  // Light warm gray
	public static final Color SAT_TABLE_ODD_ROW_BG                 = new Color(244, 242, 240);  // Slightly darker gray
	
	
	//public static final Color PACKETS_ANALYZER_PANEL_BG            = new Color(255, 250, 235);  // Cream color (warm white)
	public static final Color PACKETS_ANALYZER_PANEL_BG            = new Color(250, 248, 246);  // Light warm gray
	public static final Color PACKETS_ANALYZER_SELECTED_PANEL_BG   = new Color(216, 211, 190);  // Beige
	
	
	public static final Color PACKETS_ANALYZER_FG                  = Color.BLACK;               // Black
	public static final Color PACKETS_ANALYZER_SELECTED_FG         = new Color(255, 250, 150);  // Pale yellow
	
	public static final Color PACKETS_ANALYZER_LAYER0_BG           = new Color(230, 230, 230);  // Lightgray
	public static final Color PACKETS_ANALYZER_LAYER0_SELECTED_BG  = new Color(130, 130, 130);  // Darkgray
	
	public static final Color PACKETS_ANALYZER_LAYER1_BG           = new Color(228, 255, 200);  // Pale green
	public static final Color PACKETS_ANALYZER_LAYER1_SELECTED_BG  = new Color( 95, 140,  50);  // Darkgreen
	
	public static final Color PACKETS_ANALYZER_LAYER2_BG           = new Color(255, 228, 228);  // Pale red 
	public static final Color PACKETS_ANALYZER_LAYER2_SELECTED_BG  = new Color(164,   0,   0);  // Darkred
	
	public static final Color PACKETS_ANALYZER_LAYER3_BG           = new Color(218, 238, 255);  // Pale blue
	public static final Color PACKETS_ANALYZER_LAYER3_SELECTED_BG  = new Color( 70, 130, 185);  // Darkblue
	
	public static final Color PACKETS_ANALYZER_LAYERD_BG           = new Color(255, 230, 165);  // Pale orange
	public static final Color PACKETS_ANALYZER_LAYERD_SELECTED_BG  = new Color(200, 160,  50);  // Brown
	
	// Colors used for the texts in doc mode

	public static final Color DOC_1_1_FG                           = Color.BLACK;               // Black
	public static final Color DOC_1_2_FG                           = new Color(102, 102, 102);  // Darkgray
	public static final Color DOC_1_3_FG                           = new Color(153, 153, 153);  // Less dark gray
	public static final Color DOC_1_4_FG                           = new Color(178, 178, 178);  // Even less dark gray
	public static final Color DOC_1_5_FG                           = Color.WHITE;               // White
	
	public static final Color DOC_2_1_FG                           = new Color(255, 255,   0);  // Yellow
	public static final Color DOC_2_2_FG                           = new Color(255,   0,   0);  // Red
	public static final Color DOC_2_3_FG                           = new Color(235, 115,   0);  // Orange
	public static final Color DOC_2_4_FG                           = new Color( 79, 130, 189);  // Dark blue
	public static final Color DOC_2_5_FG                           = new Color(156, 186,  89);  // Lightgreen
	
	public static final Color DOC_3_1_FG                           = new Color(100,  75,  30);  // Darkbrown
	public static final Color DOC_3_2_FG                           = new Color(160,  25,  25);  // Darkred
	public static final Color DOC_3_3_FG                           = new Color(100,  60, 155);  // Violet
	public static final Color DOC_3_4_FG                           = new Color( 31,  74, 125);  // Marine
	public static final Color DOC_3_5_FG                           = new Color(100, 135,  20);  // Darkgreen

    // Colors used for the panels in doc mode
	
	public static final Color DOC_1_1_BG                           = Color.BLACK;               // Black
	public static final Color DOC_1_2_BG                           = new Color(205, 205, 205);  // Darkgray
	public static final Color DOC_1_3_BG                           = new Color(220, 220, 220);  // Middle gray
	public static final Color DOC_1_4_BG                           = new Color(240, 240, 240);  // Lightgray
	public static final Color DOC_1_5_BG                           = Color.WHITE;               // White
	
	public static final Color DOC_2_1_BG                           = new Color(255, 220, 220);  // Lightpink
	public static final Color DOC_2_2_BG                           = new Color(255, 210, 180);  // Salmon
	public static final Color DOC_2_3_BG                           = new Color(210, 170, 150);  // Lightbrown
	public static final Color DOC_2_4_BG                           = new Color(212, 207, 181);  // Beige
	public static final Color DOC_2_5_BG                           = new Color(245, 245, 220);  // Pale yellow
	
	public static final Color DOC_3_1_BG                           = new Color(240, 210, 240);  // Lightpurple
	public static final Color DOC_3_2_BG                           = new Color(222, 212, 255);  // Lightviolet
	public static final Color DOC_3_3_BG                           = new Color(209, 232, 255);  // Lightblue
	public static final Color DOC_3_4_BG                           = new Color(222, 255, 237);  // Lightgreen
	public static final Color DOC_3_5_BG                           = new Color(204, 222, 173);  // Kaki
	
}
