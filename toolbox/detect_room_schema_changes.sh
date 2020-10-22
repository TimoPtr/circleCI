#!/bin/bash
SCHEMA_REGEX="schemas/[a-zA-Z0-9.]*/[0-9].json"
MASTER_HASH=$(git rev-parse --short origin/master)
HEAD_HASH=$(git rev-parse --short HEAD)
PASSED=true

echo "Checking if any Room schema file was changed between commits $MASTER_HASH and $HEAD_HASH."
echo ""
echo "========= FULL DIFF ========"
echo "$(git diff --raw $MASTER_HASH $HEAD_HASH)"
echo ""
echo "======== SCHEMA DIFF ======="
echo "$(git diff --raw $MASTER_HASH $HEAD_HASH | grep $SCHEMA_REGEX)"
echo ""
echo "==== CHECK SCHEMA FILES ===="
while read -r diffedSchema ; do
  FILE=$(echo $diffedSchema | awk '{print $6}')
  ORIGINAL_FILE=$FILE
  OPERATION=$(echo $diffedSchema | awk '{print $5}')
  RESULT=true

  if [ -e $FILE ]; then continue; fi
  if [[ $OPERATION =~ ^R[0-9]* ]]; then
    FILE=$(echo $diffedSchema | awk '{print $7}')
    ORIGINAL_FILE=$(echo $diffedSchema | awk '{print $6}')
    echo " - Checking $ORIGINAL_FILE -> $FILE..."
  else
    echo " - Checking $FILE..."
  fi

  if [[ $OPERATION == R100 ]]; then
    if [[ $ORIGINAL_FILE =~ $SCHEMA_REGEX ]]; then
      ORIGINAL_FILE=${BASH_REMATCH[0]}
    fi
    if [[ $FILE =~ $SCHEMA_REGEX ]]; then
      FILE=${BASH_REMATCH[0]}
    fi
    if [ $FILE = $ORIGINAL_FILE ]; then
      echo "   [OK] file was moved to another module without modifications."
    else
      echo "   [ERROR] file was renamed!"
      RESULT=false
    fi
  elif [[ $OPERATION =~ ^R[0-9]* ]]; then
    echo "   [ERROR] file was renamed and modified!"
    RESULT=false
  else
    case $OPERATION in
      "A") echo "   [OK] new file was added." ;;
      "D") echo "   [ERROR] file was deleted!" && RESULT=false ;;
      "M") echo "   [ERROR] file was modified!" && RESULT=false ;;
      "T") echo "   [ERROR] file type was changed!" && RESULT=false ;;
      *) echo "   [OK] skipped operation ($OPERATION) performed." ;;
    esac
  fi
  if [ "$RESULT" == "false" ]; then
    PASSED=false;
  fi
done <<< "$(git diff --raw $MASTER_HASH $HEAD_HASH | grep $SCHEMA_REGEX)"

echo ""
if [ "$PASSED" == "true" ]; then
	echo "========= ALL GOOD ========="
	echo "All Room schema files are fine."
	exit 0
else
	echo "========== ERROR ==========="
	echo "Room schema file integrity check failed. Please double check your changes!."
	exit 1
fi
