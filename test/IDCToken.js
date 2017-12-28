const IDCToken = artifacts.require("IDCToken");

// have some problem
function increaseTime (duration) {
    const id = Date.now();

    return new Promise((resolve, reject) => {
        web3.currentProvider.sendAsync({
            jsonrpc: '2.0',
            method: 'evm_increaseTime',
            params: [duration],
            id: id,
        }, err1 => {
            if (err1) return reject(err1);

            web3.currentProvider.sendAsync({
                jsonrpc: '2.0',
                method: 'evm_mine',
                id: id + 1,
            }, (err2, res) => {
                return err2 ? reject(err2) : resolve(res);
            });
        });
    });
}

/**
 * Beware that due to the need of calling two separate testrpc methods and rpc calls overhead
 * it's hard to increase time precisely to a target point so design your test to tolerate
 * small fluctuations from time to time.
 *
 * @param target time in seconds
 */
function increaseTimeTo (target) {
    let now = latestTime();
    if (target < now) throw Error(`Cannot increase current time(${now}) to a moment in the past(${target})`);
    let diff = target - now;
    return increaseTime(diff);
}

const duration = {
    seconds: function (val) { return val; },
    minutes: function (val) { return val * this.seconds(60); },
    hours: function (val) { return val * this.minutes(60); },
    days: function (val) { return val * this.hours(24); },
    weeks: function (val) { return val * this.days(7); },
    years: function (val) { return val * this.days(365); },
};

function latestTime () {
    return web3.eth.getBlock('latest').timestamp;
}

const tokenName = "IDCToken";
const tokenSymbol = "IDC";
const decimalUints = 18;

const startTime = latestTime() + duration.weeks(1);
const afterStartTime = startTime + duration.seconds(10);
const endTime = startTime + duration.weeks(3);
const afterEndTime = endTime + duration.seconds(10);

const totalSupply = 8 * 10000 * 10000 * 10 ** 18;
const rate = 7000;
const moneyCap = 2400 * 10000 * 10 ** 18;
const tokenSelledCap = 3.6 * 10000 * 10000 * 10 ** 18;
// USD of one eth
const ethPrice = 718;

contract("IDCToken ico", function(accounts) {
    beforeEach(async function () {
        this.IDCTokenSale = await IDCToken.new(tokenName, tokenSymbol, decimalUints, startTime, endTime, totalSupply, rate, moneyCap, tokenSelledCap, ethPrice);
    });

    it("should right initialized", async function () {
        const actualTokenName = await this.IDCTokenSale.name();
        assert.equal(actualTokenName, tokenName, "wrong token name");

        const actualSymbol = await this.IDCTokenSale.symbol();
        assert.equal(actualSymbol, tokenSymbol, "wrong symbol");

        const actualDecinalUints = await this.IDCTokenSale.decimals();
        assert.equal(actualDecinalUints, decimalUints, "wrong decimals");

        const creatorTokens = await this.IDCTokenSale.balanceOf(accounts[0]);
        assert.equal(creatorTokens, totalSupply, "wrong amount of creator");

        const actualStartTime = await this.IDCTokenSale.startTime();
        assert.equal(actualStartTime, startTime, "wrong startTime");

        const actualEndTime = await this.IDCTokenSale.endTime();
        assert.equal(actualEndTime, endTime, "wrong endTime");

        const acutalRate = await this.IDCTokenSale.rate();
        assert.equal(acutalRate, rate, "wrong rate");

        const actualMoneyRaisedCap = await this.IDCTokenSale.moneyRaisedCap();
        assert.equal(actualMoneyRaisedCap, moneyCap, "wrong moneyRaisedCap");

        const actualTokenSelledCap = await this.IDCTokenSale.tokenSelledCap();
        assert.equal(actualTokenSelledCap, tokenSelledCap, "wrong TokenSelledCap");

        const actualEthPrice = await this.IDCTokenSale.ethPrice();
        assert.equal(actualEthPrice, ethPrice, "wrong ethPrice");
    });

    it("should allow to pause by owner", async function () {
        await this.IDCTokenSale.pause();
        const paused = await this.IDCTokenSale.paused();
        assert.equal(paused, true);
    });

    it("should allow to unpause by owner", async function () {
        await this.IDCTokenSale.pause();
        await this.IDCTokenSale.unpause();
        const paused = await this.IDCTokenSale.paused();
        assert.equal(paused, false);
    });

    it("should not allow to pause by not owner", async function() {
        try {
            await this.IDCTokenSale.pause({from: accounts[1]});
        } catch (error) {
            return;
        }

        assert.fail("should throw before");
    });

    it("should not allow to unpause by not owner", async function() {
        try {
            await this.IDCTokenSale.unpause({from: accounts[1]});
        } catch (error) {
            return;
        }

        assert.fail("should return before");
    });

    it("should send tokens to purchaser", async function() {
        const sendEther = 1;

        await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[1]});

        const tokenBalance = await this.IDCTokenSale.balanceOf(accounts[1]);
        const sellTokens = sendEther * rate * 10 ** 18;
        assert.equal(tokenBalance.valueOf(), sellTokens);
    });

    it("should allow to mint by owner(can mint)", async function() {
        const oriTokens = await this.IDCTokenSale.balanceOf(accounts[1]).valueOf();

        const success = await this.IDCTokenSale.mint(accounts[1], 10 * 10 ** 18);
        assert(success, true, "mint tokens failed");

        const nowTokens = await this.IDCTokenSale.balanceOf(accounts[1]).valueOf();
        assert(nowTokens - oriTokens, 10 * 10 ** 18, "wrong mint token amount");
    });

    it("should not allow to mint by not owner(can mint)", async function () {
        try {
            await this.IDCTokenSale.mint(accounts[2], 10 * 10 ** 18, {from: accounts[1]});
        } catch (error) {
            return;
        }

        assert.fail("should return before");
    });

    it("should not allow to mint by anyone(when finish mint)", async function () {
        const finishMint = await this.IDCTokenSale.finishMinting();
        assert(finishMint, true, "finish minting failed");

        try {
            await this.IDCTokenSale.mint(accounts[1], 10 * 10 ** 18);
        } catch (error) {
            return;
        }

        assert.fail("should return before");
    });

    it("should normal transfer tokens from account 0 to 1", async function () {

        //await increaseTimeTo(afterStartTime);

        const sendEther = 10;
        await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[0]});
        await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[1]});
        await this.IDCTokenSale.transfer(accounts[1], sendEther * rate * 10 ** 18, {from: accounts[0]});

        const tokens = await this.IDCTokenSale.balanceOf(accounts[1]).valueOf();
        assert.equal(tokens, 20 * rate *  10 ** 18, "transfer token wrong");
    });

    it("should allow to transfer from account 0 to 1(when approve)", async function() {

        await increaseTimeTo(afterStartTime);

        const sendEther = 10;
        await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[0]});
        const beforeTokens0 = await this.IDCTokenSale.balanceOf(accounts[0]).valueOf();
        const beforeTokens1 = await this.IDCTokenSale.balanceOf(accounts[1]).valueOf();

        await this.IDCTokenSale.approve(accounts[1], sendEther * rate * 10 ** 18, {from: accounts[0]});
        const allowTokens = await this.IDCTokenSale.allowance(accounts[0], accounts[1]);
        assert.equal(allowTokens, sendEther * rate * 10 ** 18, "approve wrong");

        await this.IDCTokenSale.transferFrom(accounts[0], accounts[1], sendEther * rate * 10 ** 18, {from: accounts[1]});

        const afterTokens0 = await this.IDCTokenSale.balanceOf(accounts[0]).valueOf();
        const afterTokens1 = await this.IDCTokenSale.balanceOf(accounts[1]).valueOf();
        const transferTokens0 = beforeTokens0 - afterTokens0;
        const transferTokens1 = afterTokens1 - beforeTokens1;
        assert.equal(transferTokens0, sendEther * rate * 10 ** 18, "account0: transfer from account 0 to 1 wrong");
        assert.equal(transferTokens1, sendEther * rate * 10 ** 18, "account1: transfer from account 0 to 1 wrong");
    });

    it("should not allow to transfer from account 0 to 1(when not approve)", async function () {

        //await increaseTimeTo(afterStartTime);

        const sendEther = 10;
        await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[0]});
        try {
            await this.IDCTokenSale.transferFrom(accounts[0], accounts[1], sendEther * 10 ** 18);
        } catch (error) {
            return;
        }

        assert.fail("should return before");
    })

    it("should not allow to purchase tokens when ico is over", async function () {
        const sendEther = 10;

        //await increaseTimeTo(afterEndTime);

        try {
            await this.IDCTokenSale.sendTransaction({value: web3.toWei(sendEther, "ether"), from: accounts[1]});
        } catch (error) {
            return;
        }

        assert.fail("should return before");
    });

    it("should allow to change ownership by owner", async function () {
        await this.IDCTokenSale.transferOwnership(accounts[1]);
        await this.IDCTokenSale.pause({from: accounts[1]});
        const paused = await this.IDCTokenSale.paused();
        assert.equal(paused, true);
    });
});