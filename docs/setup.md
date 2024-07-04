# Set up


isopret-gui is a desktop Java application. You can download precompiled executable
JAR files from the [Releases page](https://github.com/TheJacksonLaboratory/isopret/releases){:target="\_blank"}..
of the GitHub site. Later, we will generate stand-alone Windows and Mac native apps.
This is currently the recommended way of using isopret-gui. 

Unfortunately, currently isopretGO does not support old (intel) MacIntosh hardware.


Additionally, the following text describes how to build isopret-gui from source.


### Prerequisites


isopret-gui was written with Java version 17. If you want to
build isopret-gui from source, then the build process described below requires
[Git](https://git-scm.com/book/en/v2){:target="\_blank"}. and [maven](https://maven.apache.org/install.html){:target="\_blank"}.. (version 3.5.3 or higher).


### Installation


Go the GitHub page of [isopretGO](https://github.com/TheJacksonLaboratory/isopretGO){:target="\_blank"}., and clone the project.
Build the executable from source with maven, and then test the build.

```bash
git clone https://github.com/TheJacksonLaboratory/isopret.git
cd isopret
mvn package
java -jar target/isopret.jar
  Usage: isopret [-hV] [COMMAND]
    Isoform interpretation tool.
    -h, --help      Show this help message and exit.
    -V, --version   Print version information and exit.
  Commands:
    download, D  Download files for prositometry
    hbadeals, H  Analyze HBA-DEALS files
    svg, V       Create SVG/PDF files for a specific gene
    stats, S     Show descriptive statistics about data
```







