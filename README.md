# Mindustry-Wiki-Generator

Generates pages for the Mindustry wiki. Requires Mindustry and Arc to be in the same folder as the project to function.
Only to be used with the Mindustry CI.

## Directory Structure


- `src/wikigen`: Java source code for generating the files
  - `generators/`: Classes that define how each type of content has its pages generated.
- `templates/`: Markdown templates that are filled in by the generators. Text beginning with `$` indicates a variable that is replaced. *If a generator does not fill in a variable, the line containing it is removed.*
