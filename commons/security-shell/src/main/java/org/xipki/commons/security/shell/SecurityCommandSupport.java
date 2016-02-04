/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2013 - 2016 Lijun Liao
 * Author: Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * THE AUTHOR LIJUN LIAO. LIJUN LIAO DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the XiPKI software without
 * disclosing the source code of your own applications.
 *
 * For more information, please contact Lijun Liao at this
 * address: lijun.liao@gmail.com
 */

package org.xipki.commons.security.shell;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.xipki.commons.console.karaf.XipkiCommandSupport;
import org.xipki.commons.security.api.SecurityFactory;
import org.xipki.commons.security.api.SignerException;
import org.xipki.commons.security.api.p11.P11CryptService;
import org.xipki.commons.security.api.p11.P11Module;
import org.xipki.commons.security.api.p11.P11SlotIdentifier;
import org.xipki.commons.security.api.p11.P11WritableSlot;
import org.xipki.commons.security.p11.iaik.IaikP11CryptServiceFactory;
import org.xipki.commons.security.p11.iaik.IaikP11ModulePool;
import org.xipki.commons.security.p11.keystore.KeystoreP11CryptServiceFactory;
import org.xipki.commons.security.p11.keystore.KeystoreP11ModulePool;

/**
 * @author Lijun Liao
 * @since 2.0.0
 */

public abstract class SecurityCommandSupport extends XipkiCommandSupport {

  @Reference
  protected SecurityFactory securityFactory;

  protected P11Module getP11Module(
      final String moduleName)
  throws Exception {
    // this call initialization method
    P11CryptService p11CryptService = securityFactory.getP11CryptService(moduleName);
    if (p11CryptService == null) {
      throw new SignerException("could not initialize P11CryptService " + moduleName);
    }

    P11Module module;
    String pkcs11Provider = securityFactory.getPkcs11Provider();
    if (IaikP11CryptServiceFactory.class.getName().equals(pkcs11Provider)) {
      // the returned object could not be null
      module = IaikP11ModulePool.getInstance().getModule(moduleName);
    } else if (KeystoreP11CryptServiceFactory.class.getName().equals(pkcs11Provider)) {
      module = KeystoreP11ModulePool.getInstance().getModule(moduleName);
    } else {
      throw new SignerException("PKCS11 provider " + pkcs11Provider + " is not accepted");
    }

    return module;

  }

  protected P11WritableSlot getP11WritablSlot(
      final String moduleName,
      final int slotIndex)
  throws Exception {
    P11SlotIdentifier slotId = new P11SlotIdentifier(slotIndex, null);
    P11Module module = getP11Module(moduleName);
    if (module == null) {
      throw new SignerException("module " + moduleName + " does not exist");
    }
    P11WritableSlot slot = module.getSlot(slotId);
    if (slot == null) {
      throw new SignerException("could not get slot " + slotIndex + " of module "
          + moduleName);
    }
    return slot;
  }

}
