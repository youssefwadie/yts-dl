# yts-dl

An Unofficial Command Line Client for [YTS](https://yts.mx/) with minimal features.

1. Searching
2. Downloading - movies and subtitles (with external programs)

## Installation

1. Install JDK 17 or higher
2. Install [maven](https://maven.apache.org/download.cgi).
3. Download and package the code

```shell
git clone https://github.com/youssefwadie/yts-dl
cd yts-dl
mvn clean package
java -jar target/yts-dl-V1.0.jar -h
```

## The application has two modes

1. *Interactive*
2. *Non-Interactive*

| Mode            | Interactive Movie Search | Interactive Subtitle Search | Interactive Download |
|-----------------|--------------------------|-----------------------------|----------------------|
| Interactive     | :heavy_check_mark:       | :heavy_check_mark:          | :heavy_check_mark:   |
| Non-Interactive | :heavy_check_mark:       | :heavy_multiplication_x:    | :heavy_check_mark:   |

---

## Examples
`yts-cli [i]` -- Interactively search and download.

`yts-cli n <movie>` -- Non-Interactive search and download <movie>.

`yts-cli n harry potter --quality 720p --subtitle eng` -- search for harry potter movies and download the selected one with quality 720 and english subtitle
