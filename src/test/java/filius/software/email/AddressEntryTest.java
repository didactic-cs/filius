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
package filius.software.email;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * @author stefan
 * 
 */
public class AddressEntryTest {

	@Test
	public void onlyName() throws Exception {
		AddressEntry entry = new AddressEntry(" name ");
		assertThat(entry.getName()).isEqualTo("name");
		assertThat(entry.getMailAddress()).isNull();
		assertThat(entry.toString()).isEqualTo("name");
	}

	@Test
	public void onlyAddress() throws Exception {
		AddressEntry entry = new AddressEntry(" <address >");
		assertThat(entry.getMailAddress()).isEqualTo("address");
		assertThat(entry.getName()).isNull();
		assertThat(entry.toString()).isEqualTo("<address>");
	}

	@Test
	public void nameAndAddress() throws Exception {
		AddressEntry entry = new AddressEntry(" name    < address   > ");
		assertThat(entry.getMailAddress()).isEqualTo("address");
		assertThat(entry.getName()).isEqualTo("name");
		assertThat(entry.toString()).isEqualTo("name <address>");
	}
}
