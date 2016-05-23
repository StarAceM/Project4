# ![](https://ga-dash.s3.amazonaws.com/production/assets/logo-9f88ae6c9c3871690e33280fcf557f33.png) PEER REVIEW
# You will be reviewing projects for **two** of your peers

## Why?
- You will provide your peers with valuable feedback for their cornerstone project.
- You will receive said valuable feedback from **two** peers as well - making your cornerstone project that much better.
- You will gain insight into issues you might have on your own project.
- You will learn how to provide a semi-formal code review. Code reviews **will** be part of your job, its important to learn how they work.


## Guide to giving peer feedback

### Forking the project and preparation:
- 1. Fork your peer's repo into your github account.
- 2. Create a new ```peer_review.md``` file on **your peer project fork**. 
- 3. Copy the contents of **this** file and **paste** them into ```peer_review.md``` file you created on **your fork**.

### Creating peer review pull request:
- 1. Now you should be able to create a **pull request** from your fork back to your peer's original repo.
- 2. Title the pull request with: ```Peer review: Your Name```.
- 3. In the comments section, copy each question from below and answer it!

### Questions to answer:
##### 1. Does the project appear to meet the technical requirements? **Write up one sentence on your findings and give a score 0-3.**
- Is your peer making API calls, using SDK's/third-party libraries?
Yes, they're using the Spotify API via Retrofit with the RxJava adapter. In addition they're providing authentication via interceptors. 2/3 
- Is your peer making use of Services? If so, are they offloading long tasks to a separate thread, i.e. AsyncTask, Runnable, IntentService, etc.
Yes, they're using services to playback music with the Spotify SDK they imported. 2/3
- Is your peer making use of Fragments? If so, are they passing data from Fragment to Activity via interfaces? If not, why did absense of Fragments make sense?
Yes. 2/3
- Is your peer making use of RecyclerView? If so, does it appear to be working correctly ( implementation and otherwise )?
Yes. However, it doesn't make too much sense to use it in NavigationFragment. Perhaps, statically adding the views to a scroll view would have been simpler?
- Is your peer making use of some sort of persistent storage, i.e. Firebase or SQLite? If so, why do you think Firebase/SQLite was chosen? Could they have used one or the other instead and why?

##### 2. Does the project appear to be creative, innovative, and different from **any** competition? **Write up one sentence on your findings and give a score 0-3.**
- Is your peer making use of proper UX patterns we learned in class? If not, what are they doing that is unconvetional or that might confuse a user ( you )?
The interface is mostly custom sans the navigation drawer it seems. Considering how simple the app is in its current stat, the only things that would confuse me are probably unimplemented right now (the three buttons on the bottom). 1/3
- Is your peer making anything cool or awesome that you would like to note or applaud them on?
I like music mixer applications because right now it feels we don't have much choice on how to control our music when we're locked into our respective platforms.

##### 3. Does the project appear to follow correct coding styles and best practices? **Write up one sentence on your findings and give a score 0-3.**
- Are you able to reasonably follow the code without having anyone answer your questions?
Yes, most of the code is laid out cleanly and broken into packages. 2/3
- Are you able to make sense of what the code is doing or is trying to do?
Yes. 2/3

##### 4. Find two pieces of code of any size: one that is ```readable and easy to follow``` and one that is ```difficult to follow and understand```.
- What makes the readable code readable? **Be as detailed as you can in your answer, it can be challenging to explain why something is easy to undertand**
```java
/**
 * Takes in an ArrayList of booleans and converts it to an ArrayList of strings
 * with the names of user specified notification preferences. Puts the ArrayList of
 * strings into a comma separated string that is returned. string resource array is used
 * to make the conversion. Comma separated sting is intended to be used in SharedPreferences.
 * @return
 */
private String createNotificationString(ArrayList<Boolean> isCheckedArray){
    String strNotificationPref = "";
    String[] strArrayCategories = getResources().getStringArray(R.array.genre);
    for (int i = 0; i < isCheckedArray.size(); i++){
        if (isCheckedArray.get(i)){
            strNotificationPref = strNotificationPref + strArrayCategories[i] + ",";
        }
    }
    if (!strNotificationPref.equals("")) {
        strNotificationPref = strNotificationPref.substring(0,strNotificationPref.length()-1);
    }
    return strNotificationPref;
}
```
By providing this documentation, it made it obvious what the code was doing just by glancing at it.

- What makes the difficult code harder to follow? **Be as detailed as you can in your answer**.
```java
String strIds = "";
for (int i = 0; i < items.size(); i++) {
    if (i < items.size() - 1) {
        strIds = strIds + items.get(i).getId() + ",";
    } else {
        strIds = strIds + items.get(i).getId();
    }
}
```
I figured I could point out that this is a very common task and there's almost always a utility library for it. In this case, there's: ```TextUtils.join(",", items);```

##### 5. High level project overview: Take a look at as many individual files as you have time for
- Does this class make sense?
NaviagtionEntry, NavigationDivider, and TrackDataHelper are all empty classes.
- Does the structure of the class make sense?
- Is it clear what this class is supposed to do?
Not unless you read the NavigationAdapter class. Then it's clear the former two are used as marker types. Perhaps this could be solved in a better way; an enum perhaps?
