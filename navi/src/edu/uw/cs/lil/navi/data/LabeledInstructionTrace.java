/*******************************************************************************
 * Navi. Copyright (C) 2013 Yoav Artzi
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package edu.uw.cs.lil.navi.data;

import java.util.Map;

import edu.uw.cs.lil.navi.agent.Agent;
import edu.uw.cs.lil.navi.eval.Task;
import edu.uw.cs.lil.navi.map.NavigationMap;
import edu.uw.cs.lil.navi.map.PositionSet;
import edu.uw.cs.lil.tiny.ccg.categories.ICategoryServices;
import edu.uw.cs.lil.tiny.data.ILabeledDataItem;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.utils.composites.Pair;

/**
 * A single instruction paired with its demonstration (trace) and a logical
 * form.
 * 
 * @author Yoav Artzi
 * @param <MR>
 */
public class LabeledInstructionTrace<MR> implements
		ILabeledDataItem<Instruction, Pair<MR, Trace>> {
	
	private static final Object		MAP_NAME_KEY	= "map";
	
	private final Instruction		instruction;
	
	private final Pair<MR, Trace>	labelPair;
	
	private final MR				semantics;
	
	private final Trace				trace;
	
	public LabeledInstructionTrace(MR semantics, Sentence sentence, Task task,
			Trace trace) {
		this.instruction = new Instruction(sentence, task);
		this.semantics = semantics;
		this.trace = trace;
		this.labelPair = Pair.of(semantics, trace);
	}
	
	public static <MR> LabeledInstructionTrace<MR> parse(String string,
			Map<String, NavigationMap> maps,
			ICategoryServices<MR> categoryServices) {
		final String[] split = string.split("\n");
		final Map<String, String> properties = InstructionTrace
				.parseProperties(split[2]);
		final NavigationMap map = maps.get(properties.get(MAP_NAME_KEY));
		final Trace trace = Trace.parseLine(split[3], map);
		final Task task = new Task(new Agent(trace.getStartPosition()),
				new PositionSet(map.get(Integer.valueOf(properties.get("y")))
						.getAllOrientations(), false), new PositionSet(map.get(
						Integer.valueOf(properties.get("x")))
						.getAllOrientations(), false), properties, map);
		return new LabeledInstructionTrace<MR>(
				categoryServices.parseSemantics(split[1]), new Sentence(
						split[0]), task, trace);
	}
	
	@Override
	public double calculateLoss(Pair<MR, Trace> label) {
		return labelPair.equals(label) ? 0.0 : 1.0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final LabeledInstructionTrace other = (LabeledInstructionTrace) obj;
		if (instruction == null) {
			if (other.instruction != null) {
				return false;
			}
		} else if (!instruction.equals(other.instruction)) {
			return false;
		}
		if (semantics == null) {
			if (other.semantics != null) {
				return false;
			}
		} else if (!semantics.equals(other.semantics)) {
			return false;
		}
		if (trace == null) {
			if (other.trace != null) {
				return false;
			}
		} else if (!trace.equals(other.trace)) {
			return false;
		}
		return true;
	}
	
	@Override
	public Pair<MR, Trace> getLabel() {
		return labelPair;
	}
	
	@Override
	public Instruction getSample() {
		return instruction;
	}
	
	public Trace getTrace() {
		return trace;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instruction == null) ? 0 : instruction.hashCode());
		result = prime * result
				+ ((semantics == null) ? 0 : semantics.hashCode());
		result = prime * result + ((trace == null) ? 0 : trace.hashCode());
		return result;
	}
	
	@Override
	public boolean isCorrect(Pair<MR, Trace> label) {
		return labelPair.equals(label);
	}
	
	@Override
	public boolean prune(Pair<MR, Trace> y) {
		return !isCorrect(y);
	}
	
	@Override
	public double quality() {
		return 1.0;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(instruction.getString()).append('\n')
				.append(semantics).append('\n')
				.append(instruction.getState().propertiesToString())
				.append('\n').append(trace).toString();
	}
	
}
