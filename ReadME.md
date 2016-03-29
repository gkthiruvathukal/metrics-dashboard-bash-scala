# Github Metrics Dashboard Apache Spark Ingestion

## Objective

  This project aims to collect commit KLOC of open source projects and store the data in MongoDB with the help of Apache Spark.
  The Inital goal is to have the code working with a smaller open source project and then move on to larger projects.
  
- Ingestion Service works as follows
    1. Get the username, repository name and branch name from the frontend.
    2. Clone the repository in a temporary location, which will be deleted once the data is received
    3. Create folders for storing the respective commit object - the folder name is the commit sha for that commit
    4. Extract cloc, commit date and other required info (TODO - complete this part)
    5. 


