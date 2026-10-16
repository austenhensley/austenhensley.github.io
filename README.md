## Professional Self-Assessment
When I started the Computer Science program at Southern New Hampshire University in September 2024, I was mostly interested in cybersecurity. I was already working in IT support and wanted to understand more about the software and systems I used. Over time, I became more interested in cloud computing and engineering, especially Microsoft Azure. Between my coursework, my job, and my home lab, I realized how much I enjoy setting up systems and figuring out why something isn't working. Security is still a big part of that interest, but my goals have become more focused on building and supporting reliable systems.

Looking back, one of the biggest changes has been how I approach my work. Earlier in the program, I was usually happy once something worked. I still care about that, of course, but now I also think about whether the code makes sense, how it handles unexpected information, and what would happen if I needed to change it later. Putting this portfolio together gave me a chance to see that difference in my own work. Going back to an older project helped me notice things I probably would have overlooked when I first built it.

The SNHU Travel project helped me understand the teamwork side of software development. During the assignments, I worked through the responsibilities of a Product Owner, Developer, Tester, and Scrum Master. Seeing the project from each role helped me understand why people can have different questions about the same feature. A developer needs enough information to build it, while a tester needs to know what the finished result should look like. I came away with a better understanding of why a team needs to ask questions and share information throughout a project.

That project also helped me think about communicating with stakeholders. A request like filtering vacation options by price sounds simple at first, but there are still details to work out. What should happen if someone enters an invalid price? Which destinations should appear in the results? Writing user stories and acceptance criteria gave me practice making those expectations clear. This is something I can relate to in IT support. Before I can help someone, I need to understand what they're trying to do and where they're running into trouble.

My understanding of data structures and algorithms grew throughout the program, too. I started paying more attention to how information is organized and how much work a program repeats. Getting the right answer matters, but so does understanding how the program gets there. I also became more comfortable thinking through less obvious situations, like missing information or records that aren't in the expected order. Those details can be easy to miss when everything works with the first set of data.

Software engineering and database work taught me similar lessons about planning ahead. Keeping different responsibilities in separate parts of a project makes it easier to follow and update. With databases, I learned to look beyond whether information could be saved and retrieved. I also needed to think about how records relate to each other and what would happen to existing information after a change. These are things I pay more attention to now because a small change in one area can cause problems somewhere else.

Security has remained an interest throughout the program. In CS 305, I worked on a Spring Boot assignment that generated a SHA-256 checksum. That helped me understand how hashing can be used to check whether data has changed. My coursework built on what I had learned through Security+ and gave me more opportunities to apply it. As I move toward cloud work, I want to keep developing that habit of considering how information is protected from the start.

I've also been able to connect what I've learned to my job. One example was using PowerShell scripting and deployment through a domain controller to standardize DNS settings across more than 600 Windows machines. I had to account for differences between machines while making sure the settings were applied consistently. That work gave me a practical way to use the problem-solving skills I had been developing in school. Outside of work, my home lab gives me more opportunities to practice with virtual machines, networking, and troubleshooting. Having those experiences alongside my coursework helps me connect what I'm learning to situations I actually encounter.

For this portfolio, I chose the Weight Tracker Android app I originally created in CS 360. It lets users create an account, record their weight, set a goal, and receive a notification when they reach it. I liked using this project because it was something I could improve in several ways without losing its original purpose. Including the original version also makes it easier to see what changed and how much more thought went into the later versions.

The first enhancement focused on software design and engineering. In the original app, a lot of the work was packed into the activities. I moved database operations into repository classes and other app logic into service classes so everything had a clearer place. I also added reusable input checks, support for pounds and kilograms, and salted password hashing. Some of these changes were more involved than I expected. Adding another weight unit, for example, meant deciding how to store values consistently so older entries and goal weights would still work correctly.

The second enhancement focused on algorithms and data structures. I added a seven-entry rolling average, along with information about weight trends and progress toward a goal. Using a queue and a running total let the app calculate each new average without adding the whole group of entries again. I also had to think about what to show when someone didn't have enough entries or when their starting weight already matched their goal. The calculations were checked against a simpler method using 1,000 randomized histories. This part of the project helped me see how much thought goes into making a result both correct and useful to the person reading it.

The third enhancement focused on the database. I moved the database work to Room, improved how the tables were organized, and added date filtering and stronger checks on saved information. The most memorable part was a login problem that appeared during testing. I used Logcat to find the error and discovered that a database relationship was still pointing to a temporary table name. After fixing the migration, I checked that the saved history, goals, and preferences were still there. I also went back through the app's main features. That experience reminded me that I need to test what happens to someone who already has information saved, not just someone opening the app for the first time.

The three enhancements fit together because each one builds on the work before it. Organizing the code made it easier to add the calculations, and improving the database helped keep the information behind those calculations consistent. The code review and narratives explain why I made those changes and what I learned along the way. Together, they give someone looking at my portfolio a better idea of how I work through a problem.

Finishing this portfolio has helped me feel more comfortable talking about my technical skills. I have examples I can walk through and explain, including decisions that worked well and problems I had to fix. I still have plenty to learn as I move further into cloud computing, but I feel more prepared to take that next step. I'm bringing my IT experience with me, along with a better understanding of software and a more careful approach to making changes. I want to keep building on that and become someone a team can rely on to work through problems and keep learning.

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

