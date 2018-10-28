[![Build Status](https://travis-ci.org/HBRS-MAAS/ws18-jade-tutorials-Sushant-Chavan.svg?branch=master)](https://travis-ci.org/HBRS-MAAS/ws18-jade-tutorials-Sushant-Chavan)

# Jade Tutorials

Make sure to keep this README updated, particularly on how to run your project from the **command line**.


## Dependencies
* JADE v.4.5.0
* Java 8
* Gradle

## How to run
Just install gradle and run:

    gradle run

It will automatically get the dependencies and start JADE with the configured agents.
In case you want to clean you workspace run

    gradle clean

There are no additional steps needed to run this program. Executing the above command creates 20 buyer agents and 3 sellers. There are in total 6 different book titles, but each seller has only 4 different titles and 5 paperback copies of each. Additionally the sellers have unlimited copies of ebook versions of the paperback copies possesed by them. Each buyer agent buys 3 copies(2 ebooks and 1 paperback) of different book titles from the seller who proposes the least price for the books.

In order to change the number of sellers and buyers, Please change the variables *num_of_buyers* and *num_of_sellers* in the file *Start.java*. A console input to configure the number of buyers and sellers is not implemented to prevent Travis builds from hanging. In case it is possible to provide keyboard inputs while Travis executes, this feature could be easily implemented.

## Eclipse
To use this project with eclipse run

    gradle eclipse

This command will create the necessary eclipse files.
Afterwards you can import the project folder.
