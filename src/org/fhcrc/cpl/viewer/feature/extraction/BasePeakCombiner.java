/*
 * Copyright (c) 2003-2012 Fred Hutchinson Cancer Research Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fhcrc.cpl.viewer.feature.extraction;

import org.fhcrc.cpl.viewer.feature.extraction.strategy.BaseFeatureStrategy;

/**
 * superclass for peak combiners
 */
public abstract class BasePeakCombiner implements PeakCombiner
{
    protected int _maxCharge = PeakCombiner.DEFAULT_MAX_CHARGE;


    public int getMaxCharge()
    {
        return _maxCharge;
    }

    public void setMaxCharge(int maxCharge)
    {
        _maxCharge = maxCharge;
    }
}
