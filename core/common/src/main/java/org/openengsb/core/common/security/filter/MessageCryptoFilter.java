package org.openengsb.core.common.security.filter;

import java.util.Map;

import javax.crypto.SecretKey;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.security.MessageCryptoUtil;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.openengsb.core.common.security.AlgorithmConfig;
import org.openengsb.core.common.security.BinaryMessageCryptoUtil;
import org.openengsb.core.common.security.PrivateKeySource;

public class MessageCryptoFilter extends AbstractFilterChainElement<EncryptedMessage, byte[]> {

    private FilterAction next;

    private MessageCryptoUtil<byte[]> cryptoUtil = new BinaryMessageCryptoUtil(AlgorithmConfig.getDefault());
    private PrivateKeySource privateKeySource;

    public MessageCryptoFilter(PrivateKeySource privateKey) {
        super(EncryptedMessage.class, byte[].class);
        this.privateKeySource = privateKey;
    }

    @Override
    protected byte[] doFilter(EncryptedMessage input, Map<String, Object> metaData) {
        byte[] encryptedKey = input.getEncryptedKey();
        byte[] decryptedMessage;
        SecretKey sessionKey;
        try {
            sessionKey = cryptoUtil.decryptKey(encryptedKey, privateKeySource.getPrivateKey());
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

}
