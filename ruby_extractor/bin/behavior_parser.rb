load File.expand_path("#{File.dirname(__FILE__)}/../lib/parser.rb")

root = ARGV[0]
if ARGV[1] and ARGV[1] == 'ruby18'
  parser_type = :ruby18
  output_file = ARGV[2]
  script = File.expand_path(ARGV[3])
  script_args = ARGV[4..-1]
else
  output_file = ARGV[1]
  script = File.expand_path(ARGV[2])
  script_args = ARGV[3..-1]
end

puts script

behaviour_parser = RubyParser::BehaviourParser.new(root, parser_type)
puts "AAAAAAAAAAAAAAAAAAAa"
$LOAD_PATH.unshift(File.join("/home/jan/test/prawn/lib/"))
require "prawn"
require "benchmark"


behaviour_parser.parse_script_run!(script, script_args)
puts "BBBBBBBBBBBBBBBBBBB"

behaviour_parser.write_dynamix(output_file)
#puts behaviour_parser.version(0).trace.to_yaml




#### do something here
#### do something here
#### do something here
#### do something here

