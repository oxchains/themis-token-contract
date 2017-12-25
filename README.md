# themis-token-contract
Themis Token Contract

## Local test
<pre><code>
# start the testrpc client
testrpc
  
# start ganache-cli client
ganache-cli
 
# just need to start one of testrpc/ganache-cli 
 
# install node package 
npm install
 
# start test
truffle test
</code></pre>

## How to get tokens
* you can send eth to address of contract directly, then you will get tokens(caculate by rate setted when deploying). this will be stoped when ico over or the goal reached.
* owner can mint tokens for someone directly, and this will not be limited by totolSupplyLimit(cap of ico)

## Function of contract
* can be paused in some emergency situation
* can mint tokens to someone directly
* can sell tokens to someone automatic
* can change the owner of contract

## Testing coverage
* initialize of contract
* pause/unpause contract by owner
* can not pause/unpause contract by someone which is not owner
* sell tokens automatic
* mint tokens to someone directly by owner
* finish minting of ico
* can not buy tokens when ico is over
* change owner of contract by owner


