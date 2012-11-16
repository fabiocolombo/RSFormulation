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


instancesRoot=os.path.join("final-instances","DatiPaper1")
pathDati=os.path.join(instancesRoot,"orderedIns")
pathBins="."
pathReds=os.path.join(instancesRoot,"orderedR")
pathBigMs=os.path.join(instancesRoot,"orderedM")
pathOut="outCGHeurDatiPaper121022012"
execName="CGHeur"
solverType="s"
searchType="m"
timeout=5
maxNotSolved=5

listDati=os.listdir(pathDati);
listDati.sort();
count=0;
notSolved=-1
currentN=-1
for p in listDati:
    t=p[0];
    n=int(p[1:4]);
    if t=='n' and n!=currentN:
        currentN=n
        count=1
	notSolved=0
    else:
	count=count+1
    print("count="+str(count));
    print("notSolved="+str(notSolved));
    binStr=os.path.join(pathBins,execName)
    datiStr=os.path.join(pathDati,p)
    bigMStr=os.path.join(pathBigMs,"M-"+p)
    redStr=os.path.join(pathReds,"R-"+p)
    outputPath=os.path.join(pathOut,execName+"_"+solverType+"_out_"+p)
    of=open(outputPath,"w")
    of.write("INPUT="+p)
    print([binStr,datiStr,solverType,redStr,bigMStr,str(timeout)])
    sp=subprocess.Popen([binStr,datiStr,solverType,searchType,redStr,"0",bigMStr,str(timeout)],stdout=of)
    sp.wait()    
    of.close()
    risFile=open(outputPath,"r")
    try:
        risMap=readRisMap(risFile)
        state=risMap["STATE"]        
    except KeyError:
	state="WRONG_STATE"
    if state!="ok":
	notSolved=notSolved+1
    if notSolved>=maxNotSolved:
        print("can't solve "+str(maxNotSolved)+" instances of order "+str(n)+" exiting...")
        break
    print("state="+state)  

print("done!")
