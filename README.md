# austenhensley.github.io

## Original Artifact

Take a look at my original weight-tracking app created in CS360.

<a href="https://github.com/austenhensley/austenhensley.github.io/tree/main/Original%20Artifact" target="_blank">
View Original Artifact
</a>

## Code Review

Watch my code review covering the planned enhancements to my weight-tracking app.

<a href="https://youtu.be/Z9YHr7qq7Mc" target="_blank">
Watch Code Review
</a>

## Software Design and Engineering Enhancement
<a href="https://github.com/austenhensley/austenhensley.github.io/tree/main/Enhancement%201%20%7C%20Software%20Engineering" target="_blank">
View Enhancement 1 - Software Design and Engineering
</a>

### Narrative

For this enhancement, I went back to the weight-tracking Android app I originally made in CS 360 in C-2 2026. The app is pretty simple on purpose. Users can create an account, log in, enter their weight, set a goal weight, and get a notification once they reach that goal. I originally wanted it to be more straightforward than a lot of the bigger fitness apps that try to do everything at once.

When I first built it, I was just focused on making sure it worked. Looking back at it now, I could definitely tell that was my mindset at the time. A lot of the logic was packed into the activities, including database work and validation. The app also stored passwords in plain text, which stood out immediately as something that needed to be changed.

That is a big reason I chose this artifact for my ePortfolio. I think it shows my growth better than using something that was already polished. The original version works, but the enhanced version shows that I think more about how software is structured now and not just whether I can get a feature working.

One of the biggest changes was separating things out more. The activities are no longer doing everything themselves. Database operations were moved into repository classes, while other application logic was moved into service classes. I also added reusable validation instead of having similar checks scattered around the app. It made the project a little bigger in terms of the number of files, but the code itself makes a lot more sense to me now.

I also added support for pounds and kilograms. For that, I decided it made more sense to keep all of the stored values in pounds and just convert them when needed. I did not want the database storing a mix of units because that seemed like it could cause problems later, especially with goal weights and older entries.

The password change was probably the most important improvement. The app now uses salted password hashing instead of storing the actual password. I wanted to make sure it was really working and not just assume it was, so I created two users with the exact same password and looked at the database. They had different salts and different hashes, even though the password was the same. That was a good way to actually see that the security change was working.

I also tested the app after everything was changed. It compiled correctly in Android Studio, and I went through the main functions like creating an account, logging in, adding weight, setting a goal, and switching units. Everything still worked after the code was reorganized, which was important because I did not want the enhancement to break features that were already working.

This enhancement still covers the same course outcomes I planned for in module one. Course outcome three shows up mostly in the design decisions I had to make, like deciding how to handle the different weight units. Course outcome four is probably the strongest one here because most of this enhancement was about improving the design and structure of the software. Course outcome five was covered through the password security changes and the improvements to how data is handled. I do not think I need to change my outcome plan at this point.

The biggest thing I learned from this was probably how different it feels to go back and improve old code instead of just building something new. There were several points where the original code technically worked, so it would have been easy to leave it alone. But once I started separating things out, I could see why the original design would become harder to manage if the app kept growing.

The password update was also more involved than I originally expected. It affected login, database fields, existing data, and how passwords had to be checked. The same thing happened with unit conversion. It sounds like a small feature at first, but I still had to make decisions about how the values should actually be stored and used behind the scenes.

Overall, I think this enhancement made the artifact a lot stronger. The app still does basically the same thing it did before, but the way it is built is much better. It is cleaner, more secure, and easier to follow, and I think that shows more of what I have learned throughout the computer science program than the original version did.

## Algorithms and Data Structures
<a href="https://github.com/austenhensley/austenhensley.github.io/tree/main/Enhancement%202%20%7C%20Algorithms%20%26%20Data%20Structure" target="_blank">
View Enhancement 2 - Algorithms and Data Structures
</a>

### Narrative

For this enhancement, I continued working on the Weight Tracker Android app I originally created during CS 360 in C-2 2026. The app allows users to create an account, record their weight, set a goal, and receive a notification when they reach it. I wanted to keep it simple when I first built it. For milestone three, I added trend analysis so the recorded history gives users more insight regarding their progress.

I selected this artifact because it allows for a practical way to show how my programming skills have developed. The original version mostly stored and displayed individual measurements. Adding calculations to that history shows how I can choose data structures and algorithms to solve a specific problem. It also builds on the service and repository structure from milestone two, which gave the new calculations a clear place in the project.

The main addition is a seven-entry rolling average. I used an ArrayList to organize the entries by date and an ArrayDeque as a queue for the current window. The algorithm keeps a running total. When the window is full, it removes the oldest weight before adding the next one. That lets it calculate each new average without adding the entire window again. Each complete average is stored with its ending date and entry ID, and the two most recent averages determine whether the trend is increasing, decreasing, or stable.

Working through the efficiency helped me understand why it matters to describe the whole process. The rolling-average pass takes O(n) time, while repeatedly summing each window would take O(nk), where k is the window size. With seven fixed entries, both are linear in n, but the running total avoids repeated work. Sorting the history still takes O(n log n), so I cannot describe the entire feature as O(n). The queue uses O(k) space, while the sorted copy and stored averages bring the total space to O(n + k). Keeping the average history uses more memory, but it also preserves results that can be inspected or used for a future chart.

The dashboard now shows total weight change, the latest average, trend direction, and progress toward the current goal. Goal progress compares the change since the first entry with the change needed to reach the goal. It works for either a higher or lower goal, and the displayed percentage stays between zero and 100. The dashboard also shows the current distance from the goal. All calculations stay in pounds, which is the stored unit from milestone two, and results are converted for display when the user selects kilograms.

One challenge was deciding what the results should mean when there is not much data. Seven entries produce one complete average, but eight are needed to compare two averages. I also needed to distinguish seven entries from seven calendar days because someone could skip a day or record several measurements on one day. The dashboard explains that distinction. If the first entry already matches the goal, the percentage calculation would divide by zero, so the app explains that situation and displays the distance from the goal instead.

Date ordering was another issue. The existing dates are stored as month, day, and year text, so sorting those strings would not reliably order records across different years. I parse the dates before sorting and use the entry ID to order measurements from the same day. The analysis works on a separate collection so it does not change the history list used elsewhere. Invalid saved dates or weights produce an unavailable message instead of a misleading result.

The calculation classes were compiled and checked against a simpler averaging method using 1,000 randomized histories. These checks covered things like limited data, date ordering, and goal calculations. I also built and ran the app in Android Studio and checked the trend display, adding, editing, and deleting weights, changing goals, and switching between pounds and kilograms. Everything worked as intended, and the dashboard updated correctly after each change.

This enhancement addresses outcomes three and four, which were the primary outcomes in my Module One plan for this category. Outcome three is demonstrated through the queue, chronological ordering, sliding-window algorithm, and evaluation of time and space trade-offs. Outcome four is demonstrated by integrating those calculations into a useful dashboard feature through the existing application structure. The narrative and code comments also support outcome two. I do not need to change my outcome-coverage plan at this point.

The biggest lesson for me was that getting the average right is only part of the work. The feature also needs clear rules for missing data, repeated dates, and unusual goal values. Thinking through those cases helped me connect the algorithm to what a user will actually see and understand.

