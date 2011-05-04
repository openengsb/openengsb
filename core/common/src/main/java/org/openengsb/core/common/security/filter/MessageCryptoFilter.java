package org.openengsb.core.common.security.filter;

import java.security.PrivateKey;
import java.util.Map;

import javax.crypto.SecretKey;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedBinaryMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.openengsb.core.common.security.AlgorithmConfig;
import org.openengsb.core.common.security.BinaryMessageCryptoUtil;

public class MessageCryptoFilter extends AbstractFilterChainElement<EncryptedBinaryMessage, byte[]> {

    private FilterAction next;

    private MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());
    private PrivateKey privateKey;

    public MessageCryptoFilter() {
        super(EncryptedBinaryMessage.class, byte[].class);
    }

    @Override
    protected byte[] doFilter(EncryptedBinaryMessage input, Map<String, Object> metaData) {
        byte[] encryptedKey = input.getEncryptedKey();
        byte[] decryptedMessage;
        SecretKey sessionKey;
        try {
            sessionKey = cryptoUtil.decryptKey(encryptedKey, privateKey);
            decryptedMessage = cryptoUtil.decrypt(input.getEncryptedContent(), sessionKey);
        } catch (DecryptionException e) {
            throw new FilterException(e);
        }
        byte[] plainResult = (byte[]) next.filter(decryptedMessage, metaData);
        try {
            return cryptoUtil.encrypt(plainResult, sessionKey);
        } catch (EncryptionException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, byte[].class, byte[].class);
        this.next = next;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
