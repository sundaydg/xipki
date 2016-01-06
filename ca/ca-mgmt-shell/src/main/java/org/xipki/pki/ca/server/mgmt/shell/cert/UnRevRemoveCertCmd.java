/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 - 2016 Lijun Liao
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

package org.xipki.pki.ca.server.mgmt.shell.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.UnexpectedException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.karaf.shell.commands.Option;
import org.xipki.pki.ca.server.mgmt.api.CAEntry;
import org.xipki.pki.ca.server.mgmt.api.X509CAEntry;
import org.xipki.pki.ca.server.mgmt.shell.CaCmd;
import org.xipki.common.util.IoUtil;
import org.xipki.console.karaf.IllegalCmdParamException;
import org.xipki.security.api.util.X509Util;

/**
 * @author Lijun Liao
 */

public abstract class UnRevRemoveCertCmd extends CaCmd {
    @Option(name = "--ca",
            required = true,
            description = "CA name\n"
                    + "(required)")
    protected String caName;

    @Option(name = "--cert", aliases = "-c",
            description = "certificate file"
                    + "(either cert or serial must be specified)")
    protected String certFile;

    @Option(name = "--serial", aliases = "-s",
            description = "serial number\n"
                    + "(either cert or serial must be specified)")
    private String serialNumberS;

    protected BigInteger getSerialNumber()
    throws UnexpectedException, IllegalCmdParamException, CertificateException, IOException {
        CAEntry ca = caManager.getCA(caName);
        if (ca == null) {
            throw new UnexpectedException("CA " + caName + " not available");
        }

        if (!(ca instanceof X509CAEntry)) {
            throw new UnexpectedException("CA " + caName + " is not an X.509-CA");
        }

        BigInteger serialNumber;
        if (serialNumberS != null) {
            serialNumber = toBigInt(serialNumberS);
        } else if (certFile != null) {
            X509Certificate caCert = ((X509CAEntry) ca).getCertificate();
            X509Certificate cert = X509Util.parseCert(IoUtil.read(certFile));
            if (!X509Util.issues(caCert, cert)) {
                throw new UnexpectedException(
                        "certificate '" + certFile + "' is not issued by CA " + caName);
            }
            serialNumber = cert.getSerialNumber();
        } else {
            throw new IllegalCmdParamException("neither serialNumber nor certFile is specified");
        }

        return serialNumber;
    }

}
