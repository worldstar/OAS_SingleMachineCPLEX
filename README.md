# Goal.
We solve the order acceptance problem by using the IBM CPLEX. This repo's implementation result is shown in the following paper.

Chen, S. H., Liou, Y. C., Chen, Y. H., & Wang, K. C. (2019). Order acceptance and scheduling problem with carbon emission reduction and electricity tariffs on a single machine. Sustainability, 11(19), 5432.
https://peterchenweb.appspot.com/publications/32.sustainability-11-05432.pdf

# How to run the program
1. Download and install Eclipse and IBM CPLEX
2. Git clone this repo (or download the project and unzip the file)
3. Eclipse opens the folder of this repo.
4. Important: Modify the project setting of the library and bins for IBM CPLEX. (My settings are on my Mac. You need to modify the path of your own.)
5. Run the code of the paper: src/OASTOU/OASCarbonTaxTOUSingleObjCplex.java
