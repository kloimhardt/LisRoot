#include <stdio.h>
#include <string.h>
#include "/usr/local/include/yaml.h"

R__ADD_LIBRARY_PATH("/usr/local/lib")
R__LOAD_LIBRARY(libyaml.dylib)

int yamltest(void)
{
  FILE *fh = fopen("yamltest.yaml", "r");
  yaml_parser_t parser;
  yaml_event_t  event;   /* New variable */

  int i = 0;
  yaml_char_t *text[100];

  /* Initialize parser */
  if(!yaml_parser_initialize(&parser))
    fputs("Failed to initialize parser!\n", stderr);
  if(fh == NULL)
    fputs("Failed to open file!\n", stderr);

  /* Set input file */
  yaml_parser_set_input_file(&parser, fh);

  /* START new code */
  do {
    if (!yaml_parser_parse(&parser, &event)) {
       printf("Parser error %d\n", parser.error);
       exit(EXIT_FAILURE);
    }

    switch(event.type)
    {
    case YAML_NO_EVENT: puts("No event!"); break;
    /* Stream start/end */
    case YAML_STREAM_START_EVENT: puts("STREAM START"); break;
    case YAML_STREAM_END_EVENT:   puts("STREAM END");   break;
    /* Block delimeters */
    case YAML_DOCUMENT_START_EVENT: puts("<b>Start Document</b>"); break;
    case YAML_DOCUMENT_END_EVENT:   puts("<b>End Document</b>");   break;
    case YAML_SEQUENCE_START_EVENT: puts("<b>Start Sequence</b>"); break;
    case YAML_SEQUENCE_END_EVENT:   puts("<b>End Sequence</b>");   break;
    case YAML_MAPPING_START_EVENT:  puts("<b>Start Mapping</b>");  break;
    case YAML_MAPPING_END_EVENT:    puts("<b>End Mapping</b>");    break;
    /* Data */
    case YAML_ALIAS_EVENT:  printf("Got alias (anchor %s)\n", event.data.alias.anchor); break;
    case YAML_SCALAR_EVENT: printf("Got scalar (value %s)\n", event.data.scalar.value);
      text[i++] = event.data.scalar.value;
      break;
    }

//    if(event.type != YAML_STREAM_END_EVENT)
//      yaml_event_delete(&event);

  } while(event.type != YAML_STREAM_END_EVENT);
  yaml_event_delete(&event);
  /* END new code */

  /* Cleanup */
  yaml_parser_delete(&parser);
  fclose(fh);

  char generated_code[50];
  for(int j=0; j<i; j=j+2) {
    strcpy(generated_code,"");
    strcat(generated_code, (char*)text[j]);
    strcat(generated_code, "(\"");
    strcat(generated_code, (char*)text[j+1]);
    strcat(generated_code, "\\n\");");

    TExec *ex1 = new TExec("ex", generated_code);
    ex1->Exec();
  }

  return 0;
}
