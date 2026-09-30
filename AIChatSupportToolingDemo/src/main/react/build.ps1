cd reactfrontend
npm run build
Remove-Item -Recurse -Force "..\\..\\resources\\static\\*"
Copy-Item -Path "dist\\*" -Destination "..\\..\\resources\\static\\" -Recurse
cd ..