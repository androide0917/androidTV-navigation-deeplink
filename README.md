# androidTV-navigation-deeplink

Android TV project using leanback and Android Studio wizard, also working with navigation component and deep links

## Instructions

Run app project and in terminal use this command to test deep linking

adb shell am start -a android.intent.action.VIEW -d "https://www.example.com/movies/3"

adb shell am start -a android.intent.action.VIEW -d "https://www.example.com/movies/play/3"

### NOTE:

Demo works with deep parameter show between 0 and 4. Any other value will redirect to home screen by default
