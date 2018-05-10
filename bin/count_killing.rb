#!/usr/bin/env ruby

puts " mutant,killing.tests.length"
Dir["build/*.lst"].each do |lst|
  lines = File.readlines(lst).map{|x|x.strip}
  next if lines.length != 1_000_000
  killing_tests = lines.select{|x| x != '1'}
  STDERR.print '.'
  puts "#{lst.gsub(/build\//,'').gsub(/[.]lst/,'')},#{killing_tests.length}"
end

