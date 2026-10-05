# austenhensley.github.io

## Code Review

Watch my code review covering the planned enhancements to my weight-tracking app.

[Watch Code Review](https://youtu.be/Z9YHr7qq7Mc)

## Software Design and Engineering Enhancement

[View Enhancement 1 - Software Design and Engineering](./Enhancement%201%20%7C%20Software%20Engineering/)

### Narrative

<p style="text-indent: 40px;">
For this enhancement, I went back to the weight-tracking Android app I originally made in CS 360 in C-2 2026. The app is pretty simple on purpose. Users can create an account, log in, enter their weight, set a goal weight, and get a notification once they reach that goal. I originally wanted it to be more straightforward than a lot of the bigger fitness apps that try to do everything at once.
</p>
<p style="text-indent: 40px;">
When I first built it, I was just focused on making sure it worked. Looking back at it now, I could definitely tell that was my mindset at the time. A lot of the logic was packed into the activities, including database work and validation. The app also stored passwords in plain text, which stood out immediately as something that needed to be changed.
</p>
<p style="text-indent: 40px;">
That is a big reason I chose this artifact for my ePortfolio. I think it shows my growth better than using something that was already polished. The original version works, but the enhanced version shows that I think more about how software is structured now and not just whether I can get a feature working.
</p>
<p style="text-indent: 40px;">
One of the biggest changes was separating things out more. The activities are no longer doing everything themselves. Database operations were moved into repository classes, while other application logic was moved into service classes. I also added reusable validation instead of having similar checks scattered around the app. It made the project a little bigger in terms of the number of files, but the code itself makes a lot more sense to me now.
</p>
<p style="text-indent: 40px;">
I also added support for pounds and kilograms. For that, I decided it made more sense to keep all of the stored values in pounds and just convert them when needed. I did not want the database storing a mix of units because that seemed like it could cause problems later, especially with goal weights and older entries.
</p>
<p style="text-indent: 40px;">
The password change was probably the most important improvement. The app now uses salted password hashing instead of storing the actual password. I wanted to make sure it was really working and not just assume it was, so I created two users with the exact same password and looked at the database. They had different salts and different hashes, even though the password was the same. That was a good way to actually see that the security change was working.
</p>
<p style="text-indent: 40px;">
I also tested the app after everything was changed. It compiled correctly in Android Studio, and I went through the main functions like creating an account, logging in, adding weight, setting a goal, and switching units. Everything still worked after the code was reorganized, which was important because I did not want the enhancement to break features that were already working.
</p>
<p style="text-indent: 40px;">
This enhancement still covers the same course outcomes I planned for in module one. Course outcome three shows up mostly in the design decisions I had to make, like deciding how to handle the different weight units. Course outcome four is probably the strongest one here because most of this enhancement was about improving the design and structure of the software. Course outcome five was covered through the password security changes and the improvements to how data is handled. I do not think I need to change my outcome plan at this point.
</p>
<p style="text-indent: 40px;">
The biggest thing I learned from this was probably how different it feels to go back and improve old code instead of just building something new. There were several points where the original code technically worked, so it would have been easy to leave it alone. But once I started separating things out, I could see why the original design would become harder to manage if the app kept growing.
</p>
<p style="text-indent: 40px;">
The password update was also more involved than I originally expected. It affected login, database fields, existing data, and how passwords had to be checked. The same thing happened with unit conversion. It sounds like a small feature at first, but I still had to make decisions about how the values should actually be stored and used behind the scenes.
</p>
<p style="text-indent: 40px;">
Overall, I think this enhancement made the artifact a lot stronger. The app still does basically the same thing it did before, but the way it is built is much better. It is cleaner, more secure, and easier to follow, and I think that shows more of what I have learned throughout the computer science program than the original version did.
</p>
