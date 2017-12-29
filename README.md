# themis-token-contract
Themis Token Contract

## Local test
<pre><code>
# start ganache-cli client(simple)
# to reach goal of raised money or selled tokens, you have to set your acount enough ether
ganache-cli --account="0x1024102410241024102410241024102410241024102410241024102410241024,100000000000000000000000000" --account="0x10241024102410241024102410241024102410241410241024102410241023,100000000000000000000000000" --account="0x1024102410241024102410241024102410241024102410241024102410241022,100000000000000000000000000" --account="0x1024102410241024102410241024102410241024102410241024102410241021,100000000000000000000000000" --account="0x1024102410241024102410241024102410241024102410241024102410241020,100000000000000000000000000"
 
# install node package 
npm install
 
# start test
truffle test

# deploy contract
truffle deploy
</code></pre>

## How to get tokens
* you can send eth to address of contract directly, then you will get tokens(caculate by rate setted when deploying). this will be stoped when ico over or the goal reached.
* owner can mint tokens for someone directly, and this will not be limited by totolSupplyLimit(cap of ico)

## Function of contract
* can be paused in some emergency situation
* can mint tokens to someone directly
* can sell tokens to someone automatic
* can change the owner of contract
* will not sell tokens when goal reached
* will not sell tokens when ico over

## Testing coverage
* initialize of contract
* pause/unpause contract by owner in emergency
* can not pause/unpause contract by someone which is not owner
* sell tokens automatic
* mint tokens to someone directly by owner
* finish minting of ico
* can not buy tokens when ico is over
* change owner of contract by owner
* normal transfer/approve/transferFrom function
* will not sell tokens when goal of money raised or token selled reached
* wallet(be setted in initialize) can receive eth rightly

## process of contract

1. creator deploy contract(will set all initAmount tokens to creator's wallet)
2. buyer send eth to address of contract, then will receive IDC tokens automatic
3. ico will over(not sell tokens) when goal reached(money raised or token selled) or ico time passed by

* all transaction will be paused when in emergency


