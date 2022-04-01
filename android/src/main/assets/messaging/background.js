'use strict';
const port = browser.runtime.connectNative("browser");

console.log("background script initialized")

async function sendMessageToTab(message) {
  try {
    let tabs = await browser.tabs.query({})
    console.log(`background:tabs:${tabs}`)
    console.log(JSON.stringify(tabs));
    console.log("Message:");
    console.log(JSON.stringify(message));
    return await browser.tabs.sendMessage(
      tabs[tabs.length - 1].id,
      message
    )
  } catch (e) {
    console.log(`background:sendMessageToTab:req:error:${e}`)
    return e.toString();
  }
}



browser.runtime.onMessage.addListener((data, sender) => {
  console.log("*******BROWSER Message received")
  let action = data.action;
  console.log("background:content:onMessage:" + action);
  if (action === 'JSBridge') {
    port.postMessage(data);
  }
  return Promise.resolve('done');
})


port.onMessage.addListener(request => {
  let action = request.action;
  if (action === "evalJavascript") {
    sendMessageToTab(request).then((resp) => {
      port.postMessage(resp);
    }).catch((e) => {
      console.log(`background:sendMessageToTab:resp:error:${e}`)
    });
  }
})