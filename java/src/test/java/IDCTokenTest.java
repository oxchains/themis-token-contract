import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

public class IDCTokenTest {

    //
    String tokenName = "IDC Token";
    String tokenSymbol = "IDC";
    BigInteger decimalUints = new BigInteger("18");
    BigInteger decaimalAmount = new BigInteger("10").pow(18);
    BigInteger sleepTime = new BigInteger("60");
    BigInteger startTime = GetTime().add(sleepTime);
    BigInteger lastingTime = new BigInteger("12000").multiply(new BigInteger("60"));
    BigInteger endTime = startTime.add(lastingTime);
    BigInteger totalSupply = new BigInteger("800000000").multiply(decaimalAmount);
    BigInteger rate = new BigInteger("7000");
    BigInteger capPerAddress = new BigInteger("10").multiply(decaimalAmount);

    BigInteger gasPrice = new BigInteger("0");
    BigInteger gasLimit = new BigInteger("4500000");

    // TODO change to fit format of solidity now(length should be 10)
    public BigInteger GetTime() {

        return BigInteger.valueOf(System.currentTimeMillis()/1000);
    }

    public Web3j GetConnection(String url) {
        if ("".equals(url)) {
            return Web3j.build(new HttpService());
        }
        Web3j web3j = Web3j.build(new HttpService(url));
        return web3j;
    }

    public String DeployContract(Web3j web3j, Credentials credentials) throws Exception {
        IDCToken contract = IDCToken.deploy(
                web3j, credentials, gasPrice, gasLimit, tokenName, tokenSymbol, decimalUints,startTime, endTime, totalSupply, rate, capPerAddress, credentials.getAddress()
        ).send();
        String contractAddress = contract.getContractAddress();
        return contractAddress;
    }

    // default credentials for account 0
    public Credentials GetDefaultCredentials() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials("123", "owner.json");
        return credentials;
    }

    public Credentials GetAccount1() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials("123", "account1.json");
        return credentials;
    }

    public Credentials GetAccount2() throws Exception {
        Credentials credentials = WalletUtils.loadCredentials("123", "account2.json");
        return credentials;
    }

    public IDCToken before(Credentials credentials) throws Exception {
        //String url = "http://localhost:40404";
        String url = "http://192.168.1.115:8545";
        Web3j web3j = GetConnection(url);
        String contractAddress =  DeployContract(web3j, credentials);
        IDCToken idc = IDCToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        return idc;
    }

    public IDCToken load(String contractAddress, Credentials credentials) throws Exception {
        String url = "";
        Web3j web3j = GetConnection(url);
        IDCToken idc = IDCToken.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        return idc;
    }

    @Test
    public void TestInitialized() throws Exception {

        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        // test for initialized
        String actualCreator = idc.creator().send();
        assertEquals(actualCreator, credentials.getAddress());

        String actualTokenName = idc.name().send();
        assertEquals(actualTokenName, tokenName);

        String actualSymbol = idc.symbol().send();
        assertEquals(actualSymbol, tokenSymbol);

        BigInteger actualDecinalUints = idc.decimals().send();
        assertEquals(actualDecinalUints, decimalUints);

        BigInteger initIDC = idc.balanceOf(credentials.getAddress()).send();
        assertEquals(initIDC, totalSupply);

        BigInteger actualStartTime = idc.startTime().send();
        assertEquals(actualStartTime, startTime);

        BigInteger actualEndTime = idc.endTime().send();
        assertEquals(actualEndTime, endTime);

        BigInteger actualRate = idc.rate().send();
        assertEquals(actualRate, rate);

        BigInteger actualCapAddress = idc.capPerAddress().send();
        assertEquals(actualCapAddress, capPerAddress);

        String actualWallet = idc.wallet().send();
        assertEquals(actualWallet, credentials.getAddress());
    }

    @Test
    public void TestPause() throws Exception {

        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        // pause tokens
        idc.pause().send();

        boolean acutalState = idc.paused().send();
        assertEquals(acutalState, true);

        // unpause tokens
        idc.unpause().send();

        acutalState = idc.paused().send();
        assertEquals(acutalState, false);

        // can not be changed by not owner
        Credentials credentials1 = GetAccount1();
        IDCToken idc1 = before(credentials1);
    }

    @Test
    public void TestOwner() throws Exception {

        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        Credentials newCredentials = GetAccount1();

        idc.transferOwnership(newCredentials.getAddress()).send();
        String newOwner = idc.owner().send();
        assertEquals(newOwner, newCredentials.getAddress());
    }

    @Test
    public void TestMint() throws Exception {

        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        Credentials account1 = GetAccount1();
        BigInteger mintIDC = new BigInteger("10").multiply(decaimalAmount);

        BigInteger pre = idc.balanceOf(account1.getAddress()).send();
        idc.mint(account1.getAddress(), mintIDC).send();
        BigInteger after = idc.balanceOf(account1.getAddress()).send();

        assertEquals(after.subtract(pre), mintIDC);

        BigInteger newTotal = idc.totalSupply().send();
        assertEquals(newTotal, totalSupply.add(mintIDC));
    }

    @Test
    public void TestNormalSellToken() throws Exception {

        Web3j web3j = GetConnection("http://192.168.1.115:8545/");

        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);


        BigInteger timeNow = idc.timeNow().send();
        BigInteger actualStartTime = idc.startTime().send();
        BigInteger actualEndTime = idc.endTime().send();
        System.out.println("now is :           " + timeNow);
        System.out.println("ico start time is :" + actualStartTime);
        System.out.println("ico end time is :  " + actualEndTime);
        // increase time to start time
        timeNow = idc.timeNow().send();
        while (timeNow.compareTo(startTime) == -1) {
            timeNow = idc.timeNow().send();
        }

        System.out.println("now is:" + timeNow);

        Credentials account1 = GetAccount1();

        // add account1 to white list
        idc.addWhiteList(account1.getAddress()).send();
        System.out.println(credentials.getAddress());
        System.out.println(account1.getAddress());

        boolean inWhiteList = idc.checkExist(account1.getAddress()).send();
        assertEquals(inWhiteList, true);

        BigInteger pre = idc.balanceOf(account1.getAddress()).send();

        // account1 send eth to contract address
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                web3j, account1, idc.getContractAddress(),
                BigDecimal.valueOf(1.0), Convert.Unit.ETHER)
                .send();
        BigInteger gasUsed = transactionReceipt.getGasUsed();
        //transactionReceipt.gettr

        BigInteger after = idc.balanceOf(account1.getAddress()).send();
        System.out.println(pre);
        System.out.println(after);
        BigInteger ss = idc.balanceOf(credentials.getAddress()).send();
        System.out.println(ss);
        assertEquals(after.subtract(pre), new BigInteger("1").multiply(rate).multiply(decaimalAmount));
    }

    @Test
    public void TestNormalTransfer() throws Exception {
        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        Credentials account1 = GetAccount1();

        BigInteger pre = idc.balanceOf(account1.getAddress()).send();

        BigInteger transferIDC = new BigInteger("10").multiply(decaimalAmount);
        idc.transfer(account1.getAddress(), transferIDC).send();

        BigInteger after = idc.balanceOf(account1.getAddress()).send();
        System.out.println(after);
        assertEquals(after.subtract(pre), transferIDC);
    }

    @Test
    public void TestBurnTokens() throws Exception {
        Credentials credentials = GetDefaultCredentials();
        IDCToken idc = before(credentials);

        BigInteger pre = idc.balanceOf(credentials.getAddress()).send();
        BigInteger preTotal = idc.totalSupply().send();

        BigInteger burnAmount = new BigInteger("7000").multiply(decaimalAmount);
        idc.burn(burnAmount).send();

        BigInteger after = idc.balanceOf(credentials.getAddress()).send();
        BigInteger afterTotal = idc.totalSupply().send();

        assertEquals(pre.subtract(after), burnAmount);
        assertEquals(preTotal.subtract(afterTotal), burnAmount);
    }

    @Test
    public void TestTransferEth() throws Exception {

        Credentials account1 = GetAccount1();
        Credentials account2 = GetAccount2();

        Web3j web3j = GetConnection("");

        EthGetBalance ethGetBalance2 = web3j
                .ethGetBalance(account2.getAddress(), DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get();

        BigInteger preAccount2 = ethGetBalance2.getBalance();

        Transfer.sendFunds(
                web3j, account1, account2.getAddress(),
                BigDecimal.valueOf(1.0), Convert.Unit.ETHER)
                .send();
        BigInteger transerAmount = new BigInteger("1").multiply(decaimalAmount);

        EthGetBalance ethGetBalance4 = web3j
                .ethGetBalance(account2.getAddress(), DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get();

        BigInteger afterAccount2 = ethGetBalance4.getBalance();

        // check account2 get right amount eth
        assertEquals(transerAmount, afterAccount2.subtract(preAccount2));
    }
}
