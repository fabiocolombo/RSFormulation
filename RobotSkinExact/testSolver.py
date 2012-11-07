#!/usr/bin/python
import os
import subprocess

def readRisMap(inputFile):
    lines=inputFile.readlines()
    risMap={}
    for l in lines:
        if l.count('=') != 1:
            continue
        pair=l.split("=")
        risMap[pair[0].strip()]=pair[1].strip()
    return risMap


instancesRoot=os.path.join("..","oldInput")
pathBins="."
pathOut="outGroup05112012"
execName="exec.sh"
solverType="Group"
instanceType="OLD"
options="ROOT_MINIMIZATION_CONSTRAINTS POLYNOMIAL_ORBITOPE PFILTER"
optionsDef="#".join(options.split())
timeout=3600

listDati=os.listdir(instancesRoot);
listDati.sort();
count=0;
notSolved=-1
currentN=-1
for p in listDati:
    binStr=os.path.join(pathBins,execName)
    datiStr=os.path.join(instancesRoot,p)
    outputPath=os.path.join(pathOut,execName+"_"+solverType+"_"+optionsDef+"_out_"+p)
    of=open(outputPath,"w")
    of.write("INPUT="+p)
    exArgs=["sh",binStr,solverType,instanceType,datiStr,str(timeout),options]
    print(" ".join(exArgs))
    sp=subprocess.Popen(exArgs,stdout=of,stderr=of)
    sp.wait()    
    of.close()
    risFile=open(outputPath,"r")
    print("state="+state)  

print("done!")
