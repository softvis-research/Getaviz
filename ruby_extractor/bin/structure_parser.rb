load File.expand_path("#{File.dirname(__FILE__)}/../lib/parser.rb")

root = ARGV[0]
if ARGV[1] and ARGV[1] == 'ruby18'
  parser_type = :ruby18
  output_file = ARGV[2]
else
  output_file = ARGV[1]
end

famix_parser = RubyParser::StructureParser.new(root, parser_type)
famix_parser.write_famix(output_file)
