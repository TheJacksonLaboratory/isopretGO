


while getopts i:o: flag
  do
    case "${flag}" in
      i) input_directory=${OPTARG};;
      o) output_directory=${OPTARG};;
    esac
  done

if [ ! -d "${input_directory}" ]
then
      echo "Need to provide -i argument"
      exit 1
fi

if [ ! -d "${output_directory}" ]
then
      echo "Need to provide -o argument (create empty directory if necessary!)"
      exit 1
fi

echo "Running isopretGO from ${input_directory} and placing output in ${output_directory}"

# first build latest version
#cd ..; mvn clean package

# Get path of current file and its directory
path=`readlink -f "${BASH_SOURCE:-$0}"`
DIR_PATH=`dirname $path`
PARENT_DIR=`dirname $DIR_PATH`
JAR_PATH="$PARENT_DIR/isopret-cli/target/isopret-cli.jar"
DATA_PATH="$PARENT_DIR/data"



for entry in "$input_directory"/*
do
    if [[ $entry == *.txt ]]; then
        echo "java -jar $JAR_PATH GO -d $DATA_PATH -b $entry -o ${output_directory} --export-all"
        cd ..;java -jar $JAR_PATH GO -d $DATA_PATH -b $entry -o ${output_directory} --export-all
    fi
done