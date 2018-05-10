#!/usr/bin/env ruby

n_killed = {}
n_total = 0
mylines = STDIN.readlines()
mylines.shift # lose the header
mylines.each do |line|
  val = line.split(/,/)
  killing_tests_length = val[1].to_i
  n_killed[killing_tests_length] ||= []
  n_killed[killing_tests_length] << val[0]
  n_total += 1
  STDERR.print '.'
end

puts " ntests.k,nmutants"
n_killed.keys.sort.each do |k|
  puts "#{k},#{n_killed[k].length}"
end
