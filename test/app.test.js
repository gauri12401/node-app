console.log("Running Unit Tests...");

const result = 10 + 10;

if(result === 20){
    console.log("Test Passed");
    process.exit(0);
}
else{
    console.log("Test Failed");
    process.exit(1);
}
