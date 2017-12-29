const IDCToken = artifacts.require(`./IDCToken.sol`)

const duration = {
    seconds: function (val) { return val; },
    minutes: function (val) { return val * this.seconds(60); },
    hours: function (val) { return val * this.minutes(60); },
    days: function (val) { return val * this.hours(24); },
    weeks: function (val) { return val * this.days(7); },
    years: function (val) { return val * this.days(365); },
};

module.exports = (deployer, network, accounts) => {
  const tokenName = "IDCToken";
  const tokenSymbol = "IDC";
  const decimalUints = 18;
  const startTime = web3.eth.getBlock("latest").timestamp;
  const endTime = startTime + duration.weeks(3);
  const totalSupply = 8 * 10000 * 10000 * 10 ** 18;
  const rate = 7000;
  const moneyCap = 2400 * 1000;
  const tokenSelledCap = 3.6 * 10000 * 10000 * 10 ** 18;
  // USD of one eth
  const ethPrice = 718;
  const wallet = accounts[0];
  deployer.deploy(IDCToken, tokenName, tokenSymbol, decimalUints, startTime, endTime, totalSupply, rate, moneyCap, tokenSelledCap, ethPrice, wallet)
}
