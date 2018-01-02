import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

public class IDCTokenTest {

    public Web3j GetConnection(String url) {
        Web3j web3j = Web3j.build(new HttpService(url));
        return web3j;
    }

    public String DeployContract(Web3j web3j) throws Exception {

        BigInteger gasPrice = new BigInteger("0");
        BigInteger gasLimit = new BigInteger("4500000");

        Credentials credentials = WalletUtils.loadCredentials("test", "wallet.json");
        IDCToken contract = IDCToken.deploy(web3j, credentials, gasPrice, gasLimit).send();
        String contractAddress = contract.getContractAddress();
        return contractAddress;
    }
}
