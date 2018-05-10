#!/usr/bin/env ruby

n_killed = 0
n_total = 0
mylines = STDIN.readlines()
mylines.shift # lose the header
mylines.each do |line|
  val = line.split(/,/)
  killing_tests_length = val[1].to_i
  if killing_tests_length != 0
      n_killed += 1
  end
  n_total += 1
end

puts " #{n_killed}/#{n_total} = #{n_killed*1.0/n_total}"
